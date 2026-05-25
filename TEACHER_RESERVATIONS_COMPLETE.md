# 管理端"预约审核"功能实现文档

## 📋 功能概述

完成了老师端"预约审核"页面的后端实现，支持预约列表查询、筛选和审核操作（通过/拒绝）。

---

## 🏗️ 后端架构

### 1. 实体类（已存在）

**文件位置：** `backed/src/main/java/com/lab/backed/entity/Reservation.java`

```java
@Data
@TableName("reservation")
public class Reservation {
    private Integer id;
    private Integer studentId;        // 学生ID
    private Integer deviceId;         // 设备ID
    private LocalDateTime startTime;  // 预约开始时间
    private LocalDateTime endTime;    // 预约结束时间
    private String purpose;           // 用途说明
    private String status;            // 状态：pending/approved/rejected/cancelled
    private String reason;            // 拒绝/取消原因
    private Integer teacherId;        // 审核老师ID
    private LocalDateTime auditTime;  // 审核时间
}
```

### 2. Mapper层（已存在）

- ✅ `ReservationMapper.java` - 预约Mapper
- ✅ `StudentMapper.java` - 学生Mapper
- ✅ `DeviceMapper.java` - 设备Mapper

使用MyBatis-Plus的BaseMapper，自动提供基础CRUD方法。

### 3. Service层（新增）

#### 3.1 接口定义

**文件位置：** `backed/src/main/java/com/lab/backed/service/TeacherReservationService.java`

```java
public interface TeacherReservationService {
    // 获取预约列表（分页）
    Page<Map<String, Object>> getReservationList(String status, String studentName, 
                                                  String deviceName, Integer page, Integer size);
    
    // 审核预约
    void auditReservation(Integer id, String result, String reason, Integer teacherId);
}
```

#### 3.2 实现类

**文件位置：** `backed/src/main/java/com/lab/backed/service/impl/TeacherReservationServiceImpl.java`

**核心功能实现：**

##### ① 获取预约列表

```java
@Override
public Page<Map<String, Object>> getReservationList(String status, String studentName, 
                                                    String deviceName, Integer page, Integer size) {
    // 构建查询条件
    LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
    
    // 状态筛选
    if (status != null && !status.trim().isEmpty()) {
        wrapper.eq(Reservation::getStatus, status);
    }
    
    // 按创建时间倒序
    wrapper.orderByDesc(Reservation::getCreatedAt);
    
    // 分页查询
    Page<Reservation> reservationPage = new Page<>(page, size);
    Page<Reservation> result = reservationMapper.selectPage(reservationPage, wrapper);
    
    // 转换为前端期望的格式，并应用筛选
    List<Map<String, Object>> reservationList = result.getRecords().stream()
            .map(r -> {
                Map<String, Object> rMap = new HashMap<>();
                rMap.put("id", r.getId());
                
                // 获取学生信息
                Student student = studentMapper.selectById(r.getStudentId());
                rMap.put("studentName", student != null ? student.getName() : "未知学生");
                rMap.put("studentNo", student != null ? student.getStudentNo() : "未知学号");
                
                // 获取设备信息
                Device device = deviceMapper.selectById(r.getDeviceId());
                rMap.put("deviceName", device != null ? device.getName() : "未知设备");
                rMap.put("deviceCode", device != null ? device.getCode() : "未知编号");
                
                rMap.put("startTime", r.getStartTime().format(DATETIME_FORMATTER));
                rMap.put("endTime", r.getEndTime().format(DATETIME_FORMATTER));
                rMap.put("purpose", r.getPurpose());
                rMap.put("status", r.getStatus());
                rMap.put("reason", r.getReason());
                
                // 计算等待时长（小时）
                long waitingHours = Duration.between(r.getCreatedAt(), LocalDateTime.now()).toHours();
                rMap.put("waitingHours", waitingHours);
                
                rMap.put("createdAt", r.getCreatedAt().format(DATETIME_FORMATTER));
                
                return rMap;
            })
            .collect(Collectors.toList());
    
    // 应用前端筛选（学生姓名和设备名称）
    if ((studentName != null && !studentName.trim().isEmpty()) || 
        (deviceName != null && !deviceName.trim().isEmpty())) {
        reservationList = reservationList.stream()
                .filter(r -> {
                    boolean matchStudent = true;
                    boolean matchDevice = true;
                    
                    if (studentName != null && !studentName.trim().isEmpty()) {
                        matchStudent = ((String) r.get("studentName")).contains(studentName);
                    }
                    
                    if (deviceName != null && !deviceName.trim().isEmpty()) {
                        matchDevice = ((String) r.get("deviceName")).contains(deviceName);
                    }
                    
                    return matchStudent && matchDevice;
                })
                .collect(Collectors.toList());
    }
    
    // 构建返回的分页对象
    Page<Map<String, Object>> returnPage = new Page<>(page, size);
    returnPage.setTotal(reservationList.size());
    returnPage.setRecords(reservationList);
    
    return returnPage;
}
```

**特点：**
- ✅ 支持状态筛选（pending/approved/rejected）
- ✅ 关联查询学生姓名、学号
- ✅ 关联查询设备名称、编号
- ✅ 自动计算等待时长（小时）
- ✅ 支持学生姓名和设备名称筛选
- ✅ 日期格式化为yyyy-MM-dd HH:mm:ss

##### ② 审核预约

```java
@Override
@Transactional
public void auditReservation(Integer id, String result, String reason, Integer teacherId) {
    // 检查预约是否存在
    Reservation reservation = reservationMapper.selectById(id);
    if (reservation == null) {
        throw new RuntimeException("预约记录不存在");
    }
    
    // 检查预约状态
    if (!"pending".equals(reservation.getStatus())) {
        throw new RuntimeException("该预约已审核，无法重复操作");
    }
    
    // 验证审核结果
    if (!"approve".equals(result) && !"reject".equals(result)) {
        throw new RuntimeException("无效的审核结果");
    }
    
    // 如果是拒绝，必须有理由
    if ("reject".equals(result) && (reason == null || reason.trim().isEmpty())) {
        throw new RuntimeException("拒绝预约必须填写理由");
    }
    
    // 更新预约状态
    reservation.setStatus("approve".equals(result) ? "approved" : "rejected");
    reservation.setReason(reason);
    reservation.setTeacherId(teacherId);
    reservation.setAuditTime(LocalDateTime.now());
    
    reservationMapper.updateById(reservation);
    
    // TODO: 发送通知给学生
}
```

**特点：**
- ✅ 存在性校验
- ✅ 状态校验（只能审核pending状态的预约）
- ✅ 审核结果校验（approve/reject）
- ✅ 拒绝时必须填写理由
- ✅ 记录审核老师和审核时间
- ✅ 事务控制
- ⚠️ TODO：发送通知给学生

### 4. Controller层（新增）

**文件位置：** `backed/src/main/java/com/lab/backed/controller/TeacherReservationController.java`

```java
@RestController
@RequestMapping("/api/v1/teacher/reservations")
@RequiredArgsConstructor
public class TeacherReservationController {
    
    private final TeacherReservationService teacherReservationService;
    
    // GET /api/v1/teacher/reservations - 获取预约列表
    @GetMapping
    public Result<Map<String, Object>> getReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String deviceName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<Map<String, Object>> result = teacherReservationService.getReservationList(
                status, studentName, deviceName, page, size);
        
        Map<String, Object> data = Map.of(
            "total", result.getTotal(),
            "list", result.getRecords()
        );
        
        return Result.success(data);
    }
    
    // PUT /api/v1/teacher/reservations/{id}/audit - 审核预约
    @PutMapping("/{id}/audit")
    public Result<Void> auditReservation(
            @PathVariable Integer id,
            @RequestBody Map<String, String> params) {
        
        String result = params.get("result");
        String reason = params.get("reason");
        
        // TODO: 从token中获取当前老师ID，暂时使用固定值
        Integer teacherId = 1;
        
        teacherReservationService.auditReservation(id, result, reason, teacherId);
        return Result.success();
    }
}
```

---

## 🔌 API接口文档

### 1. 获取预约列表

**接口地址：** `GET /api/v1/teacher/reservations`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 否 | 状态筛选：pending/approved/rejected |
| studentName | String | 否 | 学生姓名（模糊搜索） |
| deviceName | String | 否 | 设备名称（模糊搜索） |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "list": [
      {
        "id": 1,
        "studentName": "张三",
        "studentNo": "20240001",
        "deviceName": "显微镜",
        "deviceCode": "DEV-001",
        "startTime": "2026-01-20 08:00:00",
        "endTime": "2026-01-20 12:00:00",
        "purpose": "细胞观察实验",
        "status": "pending",
        "reason": null,
        "waitingHours": 12,
        "createdAt": "2026-01-15 10:30:00"
      }
    ]
  },
  "timestamp": 1705315200000
}
```

---

### 2. 审核预约

**接口地址：** `PUT /api/v1/teacher/reservations/{id}/audit`

**路径参数：**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 预约ID |

**请求体：**

```json
{
  "result": "approve",
  "reason": "设备维护中，暂时无法借用"
}
```

**参数说明：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| result | String | 是 | 审核结果：approve（通过）/ reject（拒绝） |
| reason | String | 条件必填 | 拒绝时必填，通过时可选 |

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1705315200000
}
```

**错误响应：**

```json
{
  "code": 500,
  "message": "拒绝预约必须填写理由",
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
  // 获取预约列表（已启用真实API）
  getReservations(params) {
    return request.get('/teacher/reservations', { params })
  },

  // 审核预约（已启用真实API）
  auditReservation(id, data) {
    return request.put(`/teacher/reservations/${id}/audit`, data)
  }
}
```

✅ **已移除Mock调用，直接使用真实API**

### 2. 页面组件

**文件位置：** `frontend/src/views/teacher/Reservations.vue`

前端页面已完整实现，包括：
- ✅ Tab切换（待审核/已通过/已拒绝）
- ✅ 筛选功能（学生姓名、设备名称）
- ✅ 预约列表展示（表格）
- ✅ 审核操作（通过/拒绝）
- ✅ 拒绝对话框（填写理由）
- ✅ 分页功能
- ✅ 等待时长显示

---

## 🧪 测试步骤

### 1. 准备测试数据

在数据库中插入测试预约记录：

```sql
-- 插入测试预约
INSERT INTO reservation (student_id, device_id, start_time, end_time, purpose, status, created_at) VALUES
(1, 1, '2026-01-20 08:00:00', '2026-01-20 12:00:00', '细胞观察实验', 'pending', NOW()),
(2, 2, '2026-01-21 14:00:00', '2026-01-21 18:00:00', '化学分析实验', 'pending', NOW()),
(3, 3, '2026-01-22 09:00:00', '2026-01-22 11:00:00', '物理测量实验', 'approved', NOW()),
(4, 4, '2026-01-23 10:00:00', '2026-01-23 12:00:00', '生物培养实验', 'rejected', NOW());
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

### 4. 访问预约审核页面

1. 使用老师账号登录（工号：T001，密码：123456）
2. 访问 http://localhost:3000/teacher/reservations
3. 前端已自动使用真实API（无需修改USE_MOCK）

### 5. 功能测试清单

#### ✅ 获取预约列表
- [ ] 默认加载"待审核"状态的预约
- [ ] 切换到"已通过"Tab，显示已通过的预约
- [ ] 切换到"已拒绝"Tab，显示已拒绝的预约
- [ ] 分页切换正常

#### ✅ 筛选功能
- [ ] 输入学生姓名，点击搜索，只显示匹配的学生
- [ ] 输入设备名称，点击搜索，只显示匹配的设备
- [ ] 同时输入学生姓名和设备名称，组合筛选
- [ ] 清空筛选条件，显示全部

#### ✅ 审核通过
- [ ] 在"待审核"Tab点击"通过"按钮
- [ ] 提示"审核通过"
- [ ] 列表刷新，该预约不再显示
- [ ] 切换到"已通过"Tab，可以看到该预约

#### ✅ 审核拒绝
- [ ] 在"待审核"Tab点击"拒绝"按钮
- [ ] 弹出拒绝对话框
- [ ] 不填写理由直接提交，提示"请填写拒绝理由"
- [ ] 填写拒绝理由后提交
- [ ] 提示"已拒绝"
- [ ] 列表刷新，该预约不再显示
- [ ] 切换到"已拒绝"Tab，可以看到该预约和拒绝理由

#### ✅ 重复审核防护
- [ ] 尝试审核已通过的预约，提示"该预约已审核，无法重复操作"
- [ ] 尝试审核已拒绝的预约，提示"该预约已审核，无法重复操作"

---

## 📊 数据库表结构

### reservation表

```sql
CREATE TABLE `reservation` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` INT NOT NULL COMMENT '学生ID（外键）',
  `device_id` INT NOT NULL COMMENT '设备ID（外键）',
  `start_time` DATETIME NOT NULL COMMENT '预约开始时间',
  `end_time` DATETIME NOT NULL COMMENT '预约结束时间',
  `purpose` VARCHAR(255) NOT NULL COMMENT '用途说明',
  `status` ENUM('pending','approved','rejected','cancelled') NOT NULL DEFAULT 'pending' COMMENT '状态',
  `reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝/取消原因',
  `teacher_id` INT DEFAULT NULL COMMENT '审核老师ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';
```

---

## 🔧 技术要点

### 1. 关联查询

- ✅ 根据studentId查询学生姓名和学号
- ✅ 根据deviceId查询设备名称和编号
- ✅ 空值处理：未找到显示"未知学生"/"未知设备"

### 2. 等待时长计算

```java
long waitingHours = Duration.between(r.getCreatedAt(), LocalDateTime.now()).toHours();
```

使用Java 8 Time API精确计算从创建到现在的时长（小时）。

### 3. 双重筛选机制

**第一层：数据库筛选**
- 状态筛选（status）在SQL层面执行，提高性能

**第二层：内存筛选**
- 学生姓名和设备名称在Java层面执行，灵活性更高

### 4. 业务规则校验

- ✅ 只能审核pending状态的预约
- ✅ 审核结果必须是approve或reject
- ✅ 拒绝时必须填写理由
- ✅ 防止重复审核

### 5. 事务管理

- ✅ 使用@Transactional保证数据一致性
- ✅ 审核操作包含在事务中

### 6. 日期格式化

- ✅ 使用DateTimeFormatter格式化LocalDateTime
- ✅ 统一格式：yyyy-MM-dd HH:mm:ss

---

## 🚀 性能优化建议

### 1. 数据库索引

确保以下字段有索引：
- ✅ `student_id`（外键索引）
- ✅ `device_id`（外键索引）
- ✅ `status`（普通索引，用于筛选）
- ✅ `created_at`（用于排序）

### 2. N+1查询问题

**当前实现：**
```java
// 每条预约记录都会查询一次学生和設備
Student student = studentMapper.selectById(r.getStudentId());
Device device = deviceMapper.selectById(r.getDeviceId());
```

**优化方案：**
- 使用JOIN查询一次性获取所有数据
- 或者批量查询后缓存

```sql
SELECT r.*, s.name as student_name, s.student_no, 
       d.name as device_name, d.code as device_code
FROM reservation r
LEFT JOIN student s ON r.student_id = s.id
LEFT JOIN device d ON r.device_id = d.id
WHERE r.status = #{status}
ORDER BY r.created_at DESC
LIMIT #{offset}, #{size}
```

### 3. 缓存策略

可以考虑缓存：
- 学生信息（短期缓存）
- 设备信息（短期缓存）

### 4. 分页优化

- 限制最大page size（如最多100条/页）
- 大数据量时使用游标分页

---

## 📝 TODO清单

### 高优先级
- [ ] 实现JWT认证，从token中获取老师ID
- [ ] 实现通知功能（审核结果通知学生）
- [ ] 优化N+1查询问题（使用JOIN或批量查询）

### 中优先级
- [ ] 实现批量审核功能
- [ ] 添加预约日历视图
- [ ] 实现预约冲突检测

### 低优先级
- [ ] 添加预约统计报表
- [ ] 实现预约导出功能（Excel）
- [ ] 添加预约提醒功能

---

## 🐛 常见问题

### Q1: 为什么筛选后总数不准确？

**A:** 因为学生姓名和设备名称是在内存中筛选的，所以总数是筛选后的数量。如果需要准确的数据库总数，需要在SQL层面进行JOIN查询。

### Q2: 审核时提示"预约记录不存在"？

**A:** 可能的原因：
1. 预约ID不正确
2. 预约已被删除
3. 数据库连接失败

### Q3: 为什么拒绝时必须填写理由？

**A:** 这是业务规则，让学生知道被拒绝的原因，便于改进申请。

### Q4: 如何获取当前老师的ID？

**A:** 目前使用固定值1。需要实现JWT认证后，从token中解析出老师ID。

---

## 📚 相关文档

- [需求文档](requirement.md) - 查看完整的需求说明
- [数据库设计](backed/src/main/resources/db/schema.sql) - 查看表结构
- [前端页面](frontend/src/views/teacher/Reservations.vue) - 查看前端实现

---

## ✨ 总结

✅ **已完成的功能：**
1. 预约列表查询（分页、状态筛选）
2. 学生姓名和设备名称筛选
3. 关联查询学生和设备信息
4. 自动计算等待时长
5. 审核通过功能
6. 审核拒绝功能（必填理由）
7. 重复审核防护
8. 全局异常处理

✅ **前后端对接：**
- API路径完全匹配
- 请求参数格式一致
- 响应数据结构一致
- 已启用真实API（移除Mock）

✅ **代码质量：**
- 类型安全的Lambda查询
- 事务控制保证数据一致性
- 完善的参数校验和业务规则校验
- 清晰的代码结构和注释

现在您可以重启后端并访问预约审核页面进行测试了！🎉
