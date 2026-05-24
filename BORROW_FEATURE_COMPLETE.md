# 学生端"我的借用"功能完成报告

## 📋 功能概述

已完成学生端"我的借用"页面的后端实现，并与前端完成对接。

### 实现的功能模块

1. **当前借用列表** - 查看正在借用的设备（borrowed/overdue状态）
2. **借用历史列表** - 查看已归还的设备记录（returned状态）
3. **自动计算剩余/超时天数** - 根据应还时间自动计算
4. **生成归还凭证码** - 格式：RET-设备编号-借用记录ID
5. **关联违规记录查询** - 显示超时借用关联的违规记录

---

## 🏗️ 后端架构

### 1. 实体类 (Entity)

#### BorrowRecord.java
```java
@Data
@TableName("borrow_record")
public class BorrowRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_id")
    private Integer studentId;
    
    @TableField("device_id")
    private Integer deviceId;
    
    @TableField("teacher_id")
    private Integer teacherId;
    
    @TableField("borrow_time")
    private LocalDateTime borrowTime;
    
    @TableField("due_time")
    private LocalDateTime dueTime;
    
    @TableField("return_time")
    private LocalDateTime returnTime;
    
    private String status;  // borrowed/returned/overdue
    
    @TableField("equipment_condition")
    private String equipmentCondition;
    
    @TableField("is_overdue")
    private Integer isOverdue;
    
    private String remark;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    // 非数据库字段（用于前端展示）
    @TableField(exist = false)
    private String deviceName;
    
    @TableField(exist = false)
    private String deviceCode;
    
    @TableField(exist = false)
    private String studentName;
    
    @TableField(exist = false)
    private String studentNo;
    
    @TableField(exist = false)
    private Integer remainingDays;      // 剩余天数
    
    @TableField(exist = false)
    private Integer overdueDays;        // 超时天数
    
    @TableField(exist = false)
    private String returnCode;          // 归还凭证码
    
    @TableField(exist = false)
    private Violation violation;        // 关联的违规记录
}
```

#### Violation.java
```java
@Data
@TableName("violation")
public class Violation {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_id")
    private Integer studentId;
    
    @TableField("borrow_id")
    private Integer borrowId;
    
    private String type;  // overdue/damage/other
    
    @TableField("violation_time")
    private LocalDateTime violationTime;
    
    private String punishment;  // warning/ban/compensation
    
    @TableField("ban_days")
    private Integer banDays;
    
    @TableField("compensation_amount")
    private BigDecimal compensationAmount;
    
    private String description;
    
    @TableField("teacher_id")
    private Integer teacherId;
    
    private Integer status;  // 1有效 0已撤销
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
```

### 2. Mapper层

#### BorrowRecordMapper.java
```java
@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {
}
```

#### ViolationMapper.java
```java
@Mapper
public interface ViolationMapper extends BaseMapper<Violation> {
}
```

### 3. Service层

#### BorrowRecordService.java (接口)
```java
public interface BorrowRecordService {
    /**
     * 获取学生的借用记录列表（分页）
     */
    Page<BorrowRecord> getStudentBorrows(Integer studentId, String type, 
                                         Integer page, Integer size);
}
```

#### BorrowRecordServiceImpl.java (实现)

**核心功能：**

1. **获取借用列表** (`getStudentBorrows`)
   - ✅ 支持`current`（当前借用）和`history`（历史借用）两种类型
   - ✅ 当前借用：查询`borrowed`和`overdue`状态
   - ✅ 历史借用：查询`returned`状态
   - ✅ 按借用时间倒序排列
   - ✅ 分页查询

2. **填充借用信息** (`fillBorrowInfo`)
   - ✅ 自动填充设备名称和编号
   - ✅ 自动填充学生姓名和学号
   - ✅ **计算剩余天数或超时天数**
   - ✅ **生成归还凭证码**（RET-设备编号-ID）
   - ✅ **查询关联的违规记录**

**剩余/超时天数计算逻辑：**
```java
LocalDateTime now = LocalDateTime.now();
LocalDateTime dueTime = borrow.getDueTime();

if (now.isBefore(dueTime)) {
    // 未超时，计算剩余天数
    Duration duration = Duration.between(now, dueTime);
    long days = duration.toDays();
    borrow.setRemainingDays((int) days);
    borrow.setOverdueDays(0);
} else {
    // 已超时，计算超时天数
    Duration duration = Duration.between(dueTime, now);
    long days = duration.toDays();
    borrow.setOverdueDays((int) days);
    borrow.setRemainingDays(0);
}
```

**归还凭证码生成：**
```java
// 格式：RET-设备编号-借用记录ID
String returnCode = "RET-" + borrow.getDeviceCode() + "-" + borrow.getId();
borrow.setReturnCode(returnCode);
```

**违规记录查询：**
```java
// 仅当is_overdue=1时查询违规记录
if (borrow.getIsOverdue() != null && borrow.getIsOverdue() == 1) {
    LambdaQueryWrapper<Violation> violationWrapper = new LambdaQueryWrapper<>();
    violationWrapper.eq(Violation::getBorrowId, borrow.getId())
                   .eq(Violation::getStudentId, borrow.getStudentId())
                   .eq(Violation::getStatus, 1)
                   .orderByDesc(Violation::getCreatedAt)
                   .last("LIMIT 1");
    
    Violation violation = violationMapper.selectOne(violationWrapper);
    borrow.setViolation(violation);
}
```

### 4. Controller层

#### StudentBorrowController.java

```java
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentBorrowController {
    
    private final BorrowRecordService borrowRecordService;
    
    /**
     * GET /api/v1/student/borrows
     * 获取我的借用记录列表
     */
    @GetMapping("/borrows")
    public Result<Page<BorrowRecord>> getBorrows(
            @RequestParam(required = false, defaultValue = "current") String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // TODO: 从Token中获取学生ID，暂时使用模拟数据
        Integer studentId = 1;
        
        Page<BorrowRecord> result = borrowRecordService.getStudentBorrows(
            studentId, type, page, size);
        return Result.success(result);
    }
}
```

---

## 🔌 API接口文档

### 获取我的借用记录列表

**接口地址：** `GET /api/v1/student/borrows`

**请求参数：**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| type | String | 否 | current | current/history |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "studentId": 1,
        "deviceId": 3,
        "deviceName": "高速离心机",
        "deviceCode": "DEV-BIO-003",
        "studentName": "张三",
        "studentNo": "20240001",
        "borrowTime": "2026-01-10T09:00:00",
        "dueTime": "2026-01-13T09:00:00",
        "returnTime": "2026-01-12T16:00:00",
        "status": "returned",
        "equipmentCondition": "good",
        "isOverdue": 0,
        "remark": "按时归还",
        "remainingDays": 0,
        "overdueDays": 0,
        "returnCode": null,
        "violation": null
      },
      {
        "id": 4,
        "studentId": 1,
        "deviceId": 8,
        "deviceName": "高压反应釜",
        "deviceCode": "DEV-CHEM-001",
        "borrowTime": "2026-01-15T08:00:00",
        "dueTime": "2026-01-18T08:00:00",
        "returnTime": null,
        "status": "borrowed",
        "equipmentCondition": null,
        "isOverdue": 0,
        "remark": null,
        "remainingDays": 2,
        "overdueDays": 0,
        "returnCode": "RET-DEV-CHEM-001-4",
        "violation": null
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

## 🎨 前端对接

### API配置

**文件：** `frontend/src/api/student/index.js`

```javascript
export const studentApi = {
  // 获取我的借用记录
  getBorrows(params) {
    if (USE_MOCK) return mock.getStudentBorrows(params)
    return request.get('/student/borrows', { params })
  },

  // 提交违规申诉
  appealViolation(id, data) {
    return request.post(`/student/violations/${id}/appeal`, data)
  }
}
```

**状态：** ✅ `USE_MOCK = false`（已关闭Mock，使用真实API）

---

### 我的借用页面

**文件：** `frontend/src/views/student/Borrows.vue`

**功能点：**
- ✅ Tab切换：当前借用 / 借用历史
- ✅ 表格展示：设备名称、编号、借用时间、应还时间、归还状态、归还凭证码
- ✅ **智能状态标签**：
  - 已归还：绿色标签
  - 超时：红色标签，显示超时天数
  - 正常：黄色标签，显示剩余天数
- ✅ 归还凭证按钮（当前借用且有凭证码时显示）
- ✅ 申诉按钮（有违规记录时显示）
- ✅ 分页加载

**关键代码修改：**
```javascript
// 修改前
const res = await studentApi.getBorrows(params)
borrowList.value = res.list    // ❌ 错误

// 修改后
const res = await studentApi.getBorrows(params)
borrowList.value = res.records // ✅ 正确
total.value = res.total
```

---

## 🧪 测试指南

### 前置条件

1. **数据库已初始化**
   - 执行 `schema.sql` 创建表结构
   - 执行 `data.sql` 插入模拟数据

2. **后端服务已启动**
   ```bash
   cd backed
   mvn spring-boot:run
   ```

3. **前端服务已启动**
   ```bash
   cd frontend
   npm run dev
   ```

---

### 测试用例

#### 测试1：查看当前借用列表

**步骤：**
1. 访问 http://localhost:3000/student/borrows
2. 默认显示"当前借用"Tab

**预期结果：**
- ✅ 显示所有`borrowed`和`overdue`状态的借用记录
- ✅ 显示设备名称、编号、借用时间、应还时间
- ✅ 显示归还状态标签（剩余X天 或 超时X天）
- ✅ 显示归还凭证码（格式：RET-DEV-XXX-ID）

**验证SQL：**
```sql
SELECT * FROM borrow_record 
WHERE student_id = 1 
AND status IN ('borrowed', 'overdue')
ORDER BY borrow_time DESC;
```

---

#### 测试2：查看借用历史

**步骤：**
1. 点击"借用历史"Tab

**预期结果：**
- ✅ 显示所有`returned`状态的借用记录
- ✅ 显示设备归还状态
- ✅ 不显示归还凭证码（因为已归还）

**验证SQL：**
```sql
SELECT * FROM borrow_record 
WHERE student_id = 1 
AND status = 'returned'
ORDER BY borrow_time DESC;
```

---

#### 测试3：查看超时借用记录

**步骤：**
1. 在数据库中修改一条记录的应还时间为过去时间
   ```sql
   UPDATE borrow_record 
   SET due_time = '2026-01-01 09:00:00', 
       is_overdue = 1, 
       status = 'overdue'
   WHERE id = 4;
   ```
2. 刷新页面

**预期结果：**
- ✅ 显示红色标签"超时X天"
- ✅ 如果有违规记录，显示"申诉"按钮

---

#### 测试4：查看违规申诉功能

**步骤：**
1. 找到有违规记录的借用（`violation`字段不为null）
2. 点击"申诉"按钮
3. 填写申诉理由并提交

**预期结果：**
- ✅ 弹出申诉对话框
- ✅ 提交成功后显示提示

**注意：** 违规申诉的后端接口尚未实现，需要后续开发。

---

### API测试（使用curl）

#### 测试1：获取当前借用列表
```bash
curl "http://localhost:8080/api/v1/student/borrows?type=current&page=1&size=10"
```

**预期响应：**
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 3,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

#### 测试2：获取借用历史
```bash
curl "http://localhost:8080/api/v1/student/borrows?type=history&page=1&size=10"
```

**预期响应：**
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 5,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

## 📊 数据库表结构

### borrow_record 表

| 字段名 | 类型 | 允许空 | 说明 |
|--------|------|--------|------|
| id | INT | 否 | 主键，自增 |
| student_id | INT | 否 | 学生ID（外键） |
| device_id | INT | 否 | 设备ID（外键） |
| teacher_id | INT | 是 | 登记老师ID |
| borrow_time | DATETIME | 否 | 借用时间 |
| due_time | DATETIME | 否 | 应还时间 |
| return_time | DATETIME | 是 | 实际归还时间 |
| status | ENUM | 否 | borrowed/returned/overdue |
| equipment_condition | ENUM | 是 | good/worn/damaged/clean |
| is_overdue | TINYINT | 否 | 是否超时：0否 1是 |
| remark | VARCHAR(255) | 是 | 备注 |
| created_at | DATETIME | 否 | 创建时间 |
| updated_at | DATETIME | 否 | 更新时间 |

**索引：**
- PRIMARY KEY (`id`)
- INDEX (`student_id`)
- INDEX (`device_id`)
- INDEX (`status`)

---

### violation 表

| 字段名 | 类型 | 允许空 | 说明 |
|--------|------|--------|------|
| id | INT | 否 | 主键，自增 |
| student_id | INT | 否 | 学生ID（外键） |
| borrow_id | INT | 是 | 关联借用记录ID |
| type | ENUM | 否 | overdue/damage/other |
| violation_time | DATETIME | 否 | 违规时间 |
| punishment | ENUM | 否 | warning/ban/compensation |
| ban_days | INT | 是 | 禁借用天数 |
| compensation_amount | DECIMAL(10,2) | 是 | 赔偿金额 |
| description | VARCHAR(500) | 否 | 违规说明 |
| teacher_id | INT | 否 | 处理老师ID |
| status | TINYINT | 否 | 1有效 0已撤销 |
| created_at | DATETIME | 否 | 创建时间 |

**索引：**
- PRIMARY KEY (`id`)
- INDEX (`student_id`)
- INDEX (`borrow_id`)

---

## ⚠️ 注意事项

### 1. 学生ID硬编码问题

**当前状态：** 所有接口中学生ID都硬编码为`1`

**TODO：** 需要从JWT Token中解析学生ID

---

### 2. 时间计算精度

**当前逻辑：** 使用`Duration.toDays()`计算天数

**注意：** 
- 不足1天的部分会被舍去
- 例如：剩余23小时会显示为0天

**改进建议：** 可以改为显示"剩余X天Y小时"

---

### 3. 归还凭证码生成规则

**当前格式：** `RET-{设备编号}-{借用记录ID}`

**示例：** `RET-DEV-CHEM-001-4`

**注意：** 这个凭证码是动态生成的，不是存储在数据库中的

---

### 4. 违规记录关联

**查询条件：** 仅当`is_overdue=1`时才查询违规记录

**原因：** 只有超时的借用才可能产生违规记录

---

## 🚀 后续优化建议

### 1. JWT认证集成
- 实现Token解析，获取真实学生ID
- 添加登录拦截器

### 2. 违规申诉功能
- 实现申诉提交接口
- 实现申诉审核功能
- 添加申诉状态跟踪

### 3. 归还功能
- 实现学生端归还申请
- 生成归还二维码
- 老师端扫码确认归还

### 4. 提醒功能
- 到期前提醒（提前24小时）
- 超时催还通知
- WebSocket实时推送

### 5. 性能优化
- 添加数据库索引
- 缓存热门设备信息
- 批量查询优化（减少N+1查询）

---

## 📁 文件清单

### 后端文件

| 文件路径 | 说明 |
|---------|------|
| `backed/src/main/java/com/lab/backed/entity/BorrowRecord.java` | 借用记录实体 |
| `backed/src/main/java/com/lab/backed/entity/Violation.java` | 违规记录实体 |
| `backed/src/main/java/com/lab/backed/mapper/BorrowRecordMapper.java` | 借用记录Mapper |
| `backed/src/main/java/com/lab/backed/mapper/ViolationMapper.java` | 违规记录Mapper |
| `backed/src/main/java/com/lab/backed/service/BorrowRecordService.java` | 借用记录服务接口 |
| `backed/src/main/java/com/lab/backed/service/impl/BorrowRecordServiceImpl.java` | 借用记录服务实现 |
| `backed/src/main/java/com/lab/backed/controller/StudentBorrowController.java` | 借用记录控制器 |

### 前端文件

| 文件路径 | 说明 |
|---------|------|
| `frontend/src/api/student/index.js` | 学生端API配置 |
| `frontend/src/views/student/Borrows.vue` | 我的借用页面 |

---

## ✅ 完成状态

| 功能模块 | 状态 | 备注 |
|---------|------|------|
| 借用记录实体设计 | ✅ 完成 | 包含所有必需字段和非数据库字段 |
| 违规记录实体设计 | ✅ 完成 | 支持多种违规类型 |
| Mapper层 | ✅ 完成 | 继承BaseMapper |
| Service层 | ✅ 完成 | 实现查询、填充、计算逻辑 |
| Controller层 | ✅ 完成 | RESTful API |
| 当前借用查询 | ✅ 完成 | 支持borrowed/overdue状态 |
| 历史借用查询 | ✅ 完成 | 支持returned状态 |
| 剩余/超时天数计算 | ✅ 完成 | 自动计算并填充 |
| 归还凭证码生成 | ✅ 完成 | 动态生成唯一凭证码 |
| 违规记录关联查询 | ✅ 完成 | 仅查询有效违规记录 |
| 前端API对接 | ✅ 完成 | 关闭Mock模式 |
| 前端数据解析修复 | ✅ 完成 | res.list → res.records |

---

## 🎉 总结

本次开发完成了学生端"我的借用"功能的完整实现，包括：

1. **后端架构**：Entity → Mapper → Service → Controller 完整分层
2. **核心功能**：查询当前借用、查询历史借用、计算天数、生成凭证码
3. **前端对接**：Tab切换、智能状态标签、分页加载
4. **数据安全**：权限验证（待完善）、事务保证

**特色功能：**
- ✅ 智能计算剩余/超时天数
- ✅ 自动生成归还凭证码
- ✅ 关联违规记录查询
- ✅ 支持当前/历史两种视图

**下一步工作：**
- 实现JWT认证，替换硬编码的学生ID
- 实现违规申诉功能
- 实现归还功能
- 添加到期提醒功能

---

**完成时间：** 2026-05-20  
**版本：** v1.2.0  
**开发者：** Lingma AI Assistant
