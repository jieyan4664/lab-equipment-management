# 管理端"借用/归还管理"功能实现文档

## 📋 功能概述

完成了老师端"借用/归还管理"页面的后端实现，支持当前借用列表查询、借用登记、归还登记和催还通知功能。同时为筛选功能添加了重置按钮。

---

## 🏗️ 后端架构

### 1. 实体类（已存在）

**文件位置：** `backed/src/main/java/com/lab/backed/entity/BorrowRecord.java`

```java
@Data
@TableName("borrow_record")
public class BorrowRecord {
    private Integer id;
    private Integer studentId;        // 学生ID
    private Integer deviceId;         // 设备ID
    private Integer teacherId;        // 登记老师ID
    private LocalDateTime borrowTime; // 借用时间
    private LocalDateTime dueTime;    // 应还时间
    private LocalDateTime returnTime; // 实际归还时间
    private String status;            // 状态：borrowed/returned/overdue
    private String equipmentCondition;// 归还时状态：good/worn/damaged/clean
    private Integer isOverdue;        // 是否超时：0否 1是
    private String remark;            // 备注
}
```

### 2. Mapper层（已存在）

- ✅ `BorrowRecordMapper.java` - 借用记录Mapper
- ✅ `DeviceMapper.java` - 设备Mapper
- ✅ `StudentMapper.java` - 学生Mapper

### 3. Service层（新增）

#### 3.1 接口定义

**文件位置：** `backed/src/main/java/com/lab/backed/service/TeacherBorrowService.java`

```java
public interface TeacherBorrowService {
    // 获取当前借用列表
    List<Map<String, Object>> getCurrentBorrows(String keyword, Boolean isOverdue);
    
    // 借用登记
    Map<String, Object> createBorrow(String deviceCode, String studentNo, 
                                     String dueTime, String remark, Integer teacherId);
    
    // 归还登记
    void returnBorrow(String deviceCode, String equipmentCondition, 
                     String violationType, String violationDescription, Integer teacherId);
    
    // 催还通知
    void remindReturn(Integer borrowId);
}
```

#### 3.2 实现类

**文件位置：** `backed/src/main/java/com/lab/backed/service/impl/TeacherBorrowServiceImpl.java`

**核心功能实现：**

##### ① 获取当前借用列表

```java
@Override
public List<Map<String, Object>> getCurrentBorrows(String keyword, Boolean isOverdue) {
    // 构建查询条件：只查询未归还的记录
    LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
    wrapper.in(BorrowRecord::getStatus, "borrowed", "overdue")
           .orderByDesc(BorrowRecord::getBorrowTime);
    
    // 执行查询
    List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);
    
    // 转换为前端期望的格式
    List<Map<String, Object>> borrowList = records.stream()
            .map(r -> {
                Map<String, Object> rMap = new HashMap<>();
                rMap.put("id", r.getId());
                
                // 获取设备信息
                Device device = deviceMapper.selectById(r.getDeviceId());
                rMap.put("deviceName", device != null ? device.getName() : "未知设备");
                rMap.put("deviceCode", device != null ? device.getCode() : "未知编号");
                
                // 获取学生信息
                Student student = studentMapper.selectById(r.getStudentId());
                rMap.put("studentName", student != null ? student.getName() : "未知学生");
                rMap.put("studentNo", student != null ? student.getStudentNo() : "未知学号");
                
                rMap.put("borrowTime", r.getBorrowTime().format(DATETIME_FORMATTER));
                rMap.put("dueTime", r.getDueTime().format(DATETIME_FORMATTER));
                
                // 计算超时天数
                LocalDateTime now = LocalDateTime.now();
                long overdueDays = 0;
                if (r.getDueTime().isBefore(now)) {
                    overdueDays = Duration.between(r.getDueTime(), now).toDays();
                }
                rMap.put("overdueDays", overdueDays);
                
                // 状态
                String status = overdueDays > 0 ? "overdue" : "borrowed";
                rMap.put("status", status);
                
                return rMap;
            })
            .collect(Collectors.toList());
    
    // 应用筛选
    if (keyword != null && !keyword.trim().isEmpty()) {
        String kw = keyword.toLowerCase();
        borrowList = borrowList.stream()
                .filter(r -> {
                    String deviceName = ((String) r.get("deviceName")).toLowerCase();
                    String studentName = ((String) r.get("studentName")).toLowerCase();
                    return deviceName.contains(kw) || studentName.contains(kw);
                })
                .collect(Collectors.toList());
    }
    
    if (isOverdue != null) {
        borrowList = borrowList.stream()
                .filter(r -> {
                    long overdueDays = (Long) r.get("overdueDays");
                    return isOverdue ? overdueDays > 0 : overdueDays == 0;
                })
                .collect(Collectors.toList());
    }
    
    return borrowList;
}
```

**特点：**
- ✅ 只查询未归还的记录（borrowed/overdue）
- ✅ 关联查询学生和设备信息
- ✅ 自动计算超时天数
- ✅ 支持关键词筛选（设备名/学生名）
- ✅ 支持超时状态筛选
- ✅ 按借用时间倒序排列

##### ② 借用登记

```java
@Override
@Transactional
public Map<String, Object> createBorrow(String deviceCode, String studentNo, 
                                       String dueTime, String remark, Integer teacherId) {
    // 根据设备编号查找设备
    LambdaQueryWrapper<Device> deviceWrapper = new LambdaQueryWrapper<>();
    deviceWrapper.eq(Device::getCode, deviceCode);
    Device device = deviceMapper.selectOne(deviceWrapper);
    
    if (device == null) {
        throw new RuntimeException("设备不存在");
    }
    
    // 检查设备状态
    if (!"available".equals(device.getStatus())) {
        throw new RuntimeException("设备当前不可借用（状态：" + device.getStatus() + "）");
    }
    
    // 根据学号查找学生
    LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
    studentWrapper.eq(Student::getStudentNo, studentNo);
    Student student = studentMapper.selectOne(studentWrapper);
    
    if (student == null) {
        throw new RuntimeException("学生不存在");
    }
    
    // 检查学生状态
    if (student.getAccessStatus() != null && student.getAccessStatus() == 2) {
        throw new RuntimeException("学生权限已被禁用");
    }
    
    // 解析应还时间
    LocalDateTime dueTimeDT = LocalDateTime.parse(dueTime, DATETIME_FORMATTER);
    
    // 创建借用记录
    BorrowRecord borrowRecord = new BorrowRecord();
    borrowRecord.setDeviceId(device.getId());
    borrowRecord.setStudentId(student.getId());
    borrowRecord.setTeacherId(teacherId);
    borrowRecord.setBorrowTime(LocalDateTime.now());
    borrowRecord.setDueTime(dueTimeDT);
    borrowRecord.setStatus("borrowed");
    borrowRecord.setIsOverdue(0);
    borrowRecord.setRemark(remark);
    
    borrowRecordMapper.insert(borrowRecord);
    
    // 更新设备状态
    device.setStatus("borrowed");
    device.setCurrentBorrowerId(student.getId());
    device.setExpectedReturnTime(dueTimeDT);
    deviceMapper.updateById(device);
    
    // 生成归还凭证码
    String returnCode = "RET-" + String.format("%03d", device.getId()) + "-" + borrowRecord.getId();
    
    Map<String, Object> result = new HashMap<>();
    result.put("borrowId", borrowRecord.getId());
    result.put("returnCode", returnCode);
    
    return result;
}
```

**特点：**
- ✅ 设备存在性校验
- ✅ 设备状态校验（只能借用available状态的设备）
- ✅ 学生存在性校验
- ✅ 学生权限校验（禁用的学生不能借用）
- ✅ 自动生成归还凭证码（格式：RET-XXX-YYY）
- ✅ 同步更新设备状态和借用人信息
- ✅ 事务控制

##### ③ 归还登记

```java
@Override
@Transactional
public void returnBorrow(String deviceCode, String equipmentCondition, 
                        String violationType, String violationDescription, Integer teacherId) {
    // 根据设备编号查找设备
    LambdaQueryWrapper<Device> deviceWrapper = new LambdaQueryWrapper<>();
    deviceWrapper.eq(Device::getCode, deviceCode);
    Device device = deviceMapper.selectOne(deviceWrapper);
    
    if (device == null) {
        throw new RuntimeException("设备不存在");
    }
    
    // 查找该设备的当前借用记录
    LambdaQueryWrapper<BorrowRecord> borrowWrapper = new LambdaQueryWrapper<>();
    borrowWrapper.eq(BorrowRecord::getDeviceId, device.getId())
                .in(BorrowRecord::getStatus, "borrowed", "overdue")
                .orderByDesc(BorrowRecord::getBorrowTime)
                .last("LIMIT 1");
    
    BorrowRecord borrowRecord = borrowRecordMapper.selectOne(borrowWrapper);
    
    if (borrowRecord == null) {
        throw new RuntimeException("该设备没有未归还的借用记录");
    }
    
    // 更新借用记录
    borrowRecord.setReturnTime(LocalDateTime.now());
    borrowRecord.setStatus("returned");
    borrowRecord.setEquipmentCondition(equipmentCondition);
    
    // 检查是否超时
    if (borrowRecord.getDueTime().isBefore(LocalDateTime.now())) {
        borrowRecord.setIsOverdue(1);
    }
    
    borrowRecordMapper.updateById(borrowRecord);
    
    // 更新设备状态
    device.setStatus("available");
    device.setCurrentBorrowerId(null);
    device.setExpectedReturnTime(null);
    deviceMapper.updateById(device);
    
    // TODO: 如果有违规，创建违规记录
    if (violationType != null && !"none".equals(violationType)) {
        System.out.println("创建违规记录 - 类型: " + violationType + ", 说明: " + violationDescription);
    }
}
```

**特点：**
- ✅ 设备存在性校验
- ✅ 借用记录存在性校验
- ✅ 记录归还时间和设备状态
- ✅ 自动检测并标记超时
- ✅ 恢复设备为可借用状态
- ✅ 支持违规记录（TODO：需实现ViolationService）
- ✅ 事务控制

##### ④ 催还通知

```java
@Override
public void remindReturn(Integer borrowId) {
    // 查找借用记录
    BorrowRecord borrowRecord = borrowRecordMapper.selectById(borrowId);
    
    if (borrowRecord == null) {
        throw new RuntimeException("借用记录不存在");
    }
    
    // 获取学生信息
    Student student = studentMapper.selectById(borrowRecord.getStudentId());
    
    if (student == null) {
        throw new RuntimeException("学生不存在");
    }
    
    // TODO: 发送催还通知（站内信/短信）
    System.out.println("发送催还通知给学生: " + student.getName());
}
```

**特点：**
- ✅ 借用记录存在性校验
- ✅ 获取学生信息
- ⚠️ TODO：实现真实的通知功能

### 4. Controller层（新增）

**文件位置：** `backed/src/main/java/com/lab/backed/controller/TeacherBorrowController.java`

```java
@RestController
@RequestMapping("/api/v1/teacher/borrows")
@RequiredArgsConstructor
public class TeacherBorrowController {
    
    private final TeacherBorrowService teacherBorrowService;
    
    // GET /api/v1/teacher/borrows/current - 获取当前借用列表
    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentBorrows(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isOverdue) {
        
        List<Map<String, Object>> list = teacherBorrowService.getCurrentBorrows(keyword, isOverdue);
        
        Map<String, Object> data = Map.of("list", list);
        
        return Result.success(data);
    }
    
    // POST /api/v1/teacher/borrows - 借用登记
    @PostMapping
    public Result<Map<String, Object>> createBorrow(@RequestBody Map<String, String> params) {
        String deviceCode = params.get("deviceCode");
        String studentNo = params.get("studentNo");
        String dueTime = params.get("dueTime");
        String remark = params.get("remark");
        
        Integer teacherId = 1; // TODO: 从token中获取
        
        Map<String, Object> result = teacherBorrowService.createBorrow(
                deviceCode, studentNo, dueTime, remark, teacherId);
        
        return Result.success(result);
    }
    
    // POST /api/v1/teacher/borrows/return - 归还登记
    @PostMapping("/return")
    public Result<Void> returnBorrow(@RequestBody Map<String, String> params) {
        String deviceCode = params.get("deviceCode");
        String equipmentCondition = params.get("equipmentCondition");
        String violationType = params.get("violationType");
        String violationDescription = params.get("violationDescription");
        
        Integer teacherId = 1; // TODO: 从token中获取
        
        teacherBorrowService.returnBorrow(deviceCode, equipmentCondition, 
                                         violationType, violationDescription, teacherId);
        
        return Result.success();
    }
    
    // POST /api/v1/teacher/borrows/{id}/remind - 催还通知
    @PostMapping("/{id}/remind")
    public Result<Void> remindReturn(@PathVariable Integer id) {
        teacherBorrowService.remindReturn(id);
        return Result.success();
    }
}
```

---

## 🔌 API接口文档

### 1. 获取当前借用列表

**接口地址：** `GET /api/v1/teacher/borrows/current`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 关键词（设备名/学生名） |
| isOverdue | Boolean | 否 | 是否超时：true/false |

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "deviceName": "显微镜",
        "deviceCode": "DEV-001",
        "studentName": "张三",
        "studentNo": "20240001",
        "borrowTime": "2026-01-10 09:00:00",
        "dueTime": "2026-01-13 09:00:00",
        "overdueDays": 2,
        "status": "overdue"
      }
    ]
  },
  "timestamp": 1705315200000
}
```

---

### 2. 借用登记

**接口地址：** `POST /api/v1/teacher/borrows`

**请求体：**

```json
{
  "deviceCode": "DEV-001",
  "studentNo": "20240001",
  "dueTime": "2026-01-20 18:00:00",
  "remark": "细胞观察实验"
}
```

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "borrowId": 1,
    "returnCode": "RET-001-1"
  },
  "timestamp": 1705315200000
}
```

---

### 3. 归还登记

**接口地址：** `POST /api/v1/teacher/borrows/return`

**请求体：**

```json
{
  "deviceCode": "DEV-001",
  "equipmentCondition": "good",
  "violationType": "none",
  "violationDescription": ""
}
```

**参数说明：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceCode | String | 是 | 设备编号 |
| equipmentCondition | String | 是 | 设备状态：good/worn/damaged/clean |
| violationType | String | 否 | 违规类型：none/overdue/damage |
| violationDescription | String | 条件必填 | 违规说明（有违规时必填） |

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1705315200000
}
```

---

### 4. 催还通知

**接口地址：** `POST /api/v1/teacher/borrows/{id}/remind`

**路径参数：**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 借用记录ID |

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1705315200000
}
```

---

## 🎨 前端对接

### 1. API配置（已修改）

**文件位置：** `frontend/src/api/teacher/index.js`

```javascript
export const teacherApi = {
  // 借用登记（已启用真实API）
  createBorrow(data) {
    return request.post('/teacher/borrows', data)
  },

  // 归还登记（已启用真实API）
  returnBorrow(data) {
    return request.post('/teacher/borrows/return', data)
  },

  // 获取当前借用列表（已启用真实API）
  getCurrentBorrows(params) {
    return request.get('/teacher/borrows/current', { params })
  },

  // 催还通知
  remindReturn(id) {
    return request.post(`/teacher/borrows/${id}/remind`)
  }
}
```

✅ **已移除Mock调用，直接使用真实API**

### 2. 页面组件

**文件位置：** `frontend/src/views/teacher/Borrows.vue`

前端页面已完整实现，包括：
- ✅ 当前借用列表展示
- ✅ 筛选功能（关键词、超时状态）
- ✅ **重置按钮**（新增）
- ✅ 借用登记对话框
- ✅ 归还登记对话框
- ✅ 催还通知功能
- ✅ 超时天数显示

### 3. 重置功能（新增）

```javascript
const handleReset = () => {
  keyword.value = ''
  isOverdue.value = null
  loadBorrows()
}
```

**作用：**
- 清空关键词筛选条件
- 清空超时状态筛选
- 重新加载完整列表

---

## 🧪 测试步骤

### 1. 准备测试数据

在数据库中插入测试数据：

```sql
-- 插入测试借用记录
INSERT INTO borrow_record (student_id, device_id, teacher_id, borrow_time, due_time, status, is_overdue) VALUES
(1, 1, 1, '2026-01-10 09:00:00', '2026-01-13 09:00:00', 'borrowed', 0),
(2, 2, 1, '2026-01-08 10:00:00', '2026-01-11 10:00:00', 'overdue', 1);
```

### 2. 启动后端服务

```bash
cd backed
mvn spring-boot:run
```

### 3. 启动前端服务

```bash
cd frontend
npm run dev
```

### 4. 访问借用归还管理页面

1. 使用老师账号登录（工号：T001，密码：123456）
2. 访问 http://localhost:3000/teacher/borrows
3. **无需修改任何配置**，已自动使用真实API

### 5. 功能测试清单

#### ✅ 获取当前借用列表
- [ ] 默认加载所有未归还的借用记录
- [ ] 显示设备名称、编号、学生姓名、学号
- [ ] 显示借用时间、应还时间
- [ ] 自动计算并显示超时天数
- [ ] 超时记录显示红色标签

#### ✅ 筛选功能
- [ ] 输入关键词（设备名或学生名），点击搜索
- [ ] 选择超时状态（是/否），点击搜索
- [ ] 组合筛选（关键词+超时状态）
- [ ] **点击重置按钮，清除所有筛选条件**

#### ✅ 借用登记
- [ ] 点击"借用登记"按钮打开对话框
- [ ] 输入设备编号、学生学号、应还时间
- [ ] 提交成功，提示"借用登记成功"
- [ ] 列表刷新，显示新借用记录
- [ ] 设备状态更新为"borrowed"
- [ ] 生成归还凭证码

#### ✅ 归还登记
- [ ] 点击"归还登记"按钮打开对话框
- [ ] 输入设备编号
- [ ] 选择设备状态（正常/磨损/损坏/需清洁）
- [ ] 如有违规，选择违规类型并填写说明
- [ ] 提交成功，提示"归还登记成功"
- [ ] 列表刷新，该记录不再显示
- [ ] 设备状态恢复为"available"

#### ✅ 催还通知
- [ ] 点击"催还"按钮
- [ ] 提示"催还通知已发送"
- [ ] （TODO）学生收到通知

---

## 📊 数据库表结构

### borrow_record表

```sql
CREATE TABLE `borrow_record` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` INT NOT NULL COMMENT '学生ID（外键）',
  `device_id` INT NOT NULL COMMENT '设备ID（外键）',
  `teacher_id` INT DEFAULT NULL COMMENT '登记老师ID',
  `borrow_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借用时间',
  `due_time` DATETIME NOT NULL COMMENT '应还时间',
  `return_time` DATETIME DEFAULT NULL COMMENT '实际归还时间',
  `status` ENUM('borrowed','returned','overdue') NOT NULL DEFAULT 'borrowed' COMMENT '状态',
  `equipment_condition` ENUM('good','worn','damaged','clean') DEFAULT NULL COMMENT '归还时状态',
  `is_overdue` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超时：0否 1是',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_status` (`status`),
  KEY `idx_borrow_time` (`borrow_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借用记录表';
```

---

## 🔧 技术要点

### 1. 业务规则校验

- ✅ 借用时检查设备状态（只能借用available）
- ✅ 借用时检查学生权限（禁用的学生不能借用）
- ✅ 归还时检查借用记录存在性
- ✅ 自动检测超时并标记

### 2. 数据一致性

- ✅ 借用时同步更新设备状态和借用人
- ✅ 归还时同步恢复设备状态
- ✅ 使用@Transactional保证事务一致性

### 3. 超时计算

```java
LocalDateTime now = LocalDateTime.now();
long overdueDays = 0;
if (r.getDueTime().isBefore(now)) {
    overdueDays = Duration.between(r.getDueTime(), now).toDays();
}
```

使用Java 8 Time API精确计算超时天数。

### 4. 归还凭证码生成

```java
String returnCode = "RET-" + String.format("%03d", device.getId()) + "-" + borrowRecord.getId();
// 示例：RET-001-123
```

### 5. 筛选逻辑

- 关键词筛选：模糊匹配设备名或学生名
- 超时筛选：根据overdueDays判断

---

## 🚀 性能优化建议

### 1. 数据库索引

确保以下字段有索引：
- ✅ `student_id`（外键索引）
- ✅ `device_id`（外键索引）
- ✅ `status`（普通索引，用于筛选）
- ✅ `borrow_time`（用于排序）

### 2. N+1查询问题

**当前实现：**
```java
// 每条借用记录都会查询一次学生和設備
Student student = studentMapper.selectById(r.getStudentId());
Device device = deviceMapper.selectById(r.getDeviceId());
```

**优化方案：**
- 使用JOIN查询一次性获取所有数据
- 或者批量查询后缓存

### 3. 缓存策略

可以考虑缓存：
- 学生信息（短期缓存）
- 设备信息（短期缓存）

---

## 📝 TODO清单

### 高优先级
- [ ] 实现JWT认证，从token中获取老师ID
- [ ] 实现通知功能（催还通知、归还通知）
- [ ] 优化N+1查询问题（使用JOIN或批量查询）
- [ ] 实现违规记录创建功能

### 中优先级
- [ ] 添加借用历史记录查询
- [ ] 实现批量归还功能
- [ ] 添加借用统计报表

### 低优先级
- [ ] 实现二维码扫码借用/归还
- [ ] 添加借用提醒功能
- [ ] 实现借用导出功能（Excel）

---

## 🐛 常见问题

### Q1: 借用时提示"设备当前不可借用"？

**A:** 可能的原因：
1. 设备状态不是"available"（可能是borrowed/repair/scrap）
2. 设备正在被其他人借用

### Q2: 归还时提示"该设备没有未归还的借用记录"？

**A:** 可能的原因：
1. 设备编号输入错误
2. 该设备已经被归还
3. 该设备从未被借用

### Q3: 如何查看超时的借用记录？

**A:** 在筛选栏中选择"超时：是"，然后点击"搜索"按钮。

### Q4: 重置按钮的作用是什么？

**A:** 清除所有筛选条件（关键词、超时状态），重新加载完整的借用列表。

---

## ✨ 总结

✅ **已完成的功能：**
1. 当前借用列表查询（只显示未归还记录）
2. 关键词筛选（设备名/学生名）
3. 超时状态筛选
4. **重置按钮**（清除筛选条件）
5. 借用登记（含完整校验）
6. 归还登记（含设备状态记录）
7. 催还通知（基础实现）
8. 自动生成归还凭证码
9. 自动计算超时天数
10. 全局异常处理

✅ **前后端对接：**
- API路径完全匹配
- 请求参数格式一致
- 响应数据结构一致
- 已启用真实API（移除Mock）

✅ **代码质量：**
- 完善的业务规则校验
- 事务控制保证数据一致性
- 清晰的代码结构和注释
- 空值安全处理

现在您可以重启后端并访问借用归还管理页面进行测试了！🎉
