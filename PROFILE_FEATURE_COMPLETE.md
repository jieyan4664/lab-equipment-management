# 学生端"个人中心"功能实现文档

## 📋 功能概述

完成了学生端"个人中心"页面的后端实现，包括：
- ✅ 获取学生个人资料（姓名、学号、班级、电话、邮箱、准入状态等）
- ✅ 获取违规记录列表（包含设备名称、处理老师等详细信息）
- ✅ 更新个人资料（电话、邮箱）
- ✅ 时间格式统一使用 ISO 8601 标准（`yyyy-MM-dd'T'HH:mm:ss`）
- ✅ 数据格式与前端完全匹配

---

## 🏗️ 后端架构

### 创建的文件清单

#### 1. 实体类（Entity）
- **Teacher.java** - 老师实体
  - 路径：`backed/src/main/java/com/lab/backed/entity/Teacher.java`
  - 字段：id, teacherNo, name, phone, email, password, role, status, createdAt, updatedAt

#### 2. Mapper接口
- **TeacherMapper.java** - 老师数据访问层
  - 路径：`backed/src/main/java/com/lab/backed/mapper/TeacherMapper.java`
  - 继承：`BaseMapper<Teacher>`

#### 3. Service层
- **ProfileService.java** - 个人中心服务接口
  - 路径：`backed/src/main/java/com/lab/backed/service/ProfileService.java`
  - 方法：
    - `getStudentProfile(Integer studentId)` - 获取学生资料
    - `updateStudentProfile(Integer studentId, Map<String, String> updates)` - 更新资料

- **ProfileServiceImpl.java** - 个人中心服务实现
  - 路径：`backed/src/main/java/com/lab/backed/service/impl/ProfileServiceImpl.java`
  - 核心功能：
    - 查询学生基本信息
    - 查询违规记录列表
    - 关联查询设备名称（通过borrow_id → borrow_record → device）
    - 关联查询老师姓名（通过teacher_id → teacher）
    - 时间格式化（ISO 8601标准）
    - 更新学生电话和邮箱

#### 4. Controller层
- **StudentProfileController.java** - 学生个人中心控制器
  - 路径：`backed/src/main/java/com/lab/backed/controller/StudentProfileController.java`
  - 接口：
    - `GET /api/v1/student/profile` - 获取个人资料
    - `PUT /api/v1/student/profile` - 更新个人资料

---

## 📡 API接口文档

### 1. 获取个人中心数据

**接口地址：** `GET /api/v1/student/profile`

**请求参数：** 无（学生ID从Token中获取，当前使用模拟数据studentId=1）

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "student": {
      "name": "张三",
      "studentNo": "20240001",
      "class": "生物技术1班",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "accessStatus": "normal",
      "accessExpire": "2026-12-31"
    },
    "violations": [
      {
        "id": 1,
        "time": "2025-12-20T14:30:00",
        "deviceName": "离心机",
        "type": "overdue",
        "punishment": "warning",
        "teacherName": "李老师"
      }
    ]
  },
  "timestamp": 1705315200000
}
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| student.name | String | 学生姓名 |
| student.studentNo | String | 学号 |
| student.class | String | 班级 |
| student.phone | String | 联系电话 |
| student.email | String | 邮箱 |
| student.accessStatus | String | 准入状态：normal/disabled |
| student.accessExpire | String | 准入有效期（格式：yyyy-MM-dd） |
| violations | Array | 违规记录列表 |
| violations[].id | Integer | 违规记录ID |
| violations[].time | String | 违规时间（格式：yyyy-MM-dd'T'HH:mm:ss） |
| violations[].deviceName | String | 设备名称 |
| violations[].type | String | 违规类型：overdue/damage/other |
| violations[].punishment | String | 处罚措施：warning/ban/compensation |
| violations[].teacherName | String | 处理老师姓名 |

---

### 2. 更新个人资料

**接口地址：** `PUT /api/v1/student/profile`

**请求参数：**
```json
{
  "phone": "13900139000",
  "email": "newemail@example.com"
}
```

**参数说明：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 否 | 联系电话 |
| email | String | 否 | 邮箱 |

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
  "code": 400,
  "message": "学生不存在",
  "data": null,
  "timestamp": 1705315200000
}
```

---

## 🔧 技术要点

### 1. 时间格式规范（重要！）

✅ **正确做法：**
```java
// 日期格式（用于accessExpire）
private static final DateTimeFormatter DATE_FORMATTER = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd");

// 日期时间格式（用于violationTime）
private static final DateTimeFormatter DATETIME_FORMATTER = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

// 使用示例
vMap.put("time", v.getViolationTime().format(DATETIME_FORMATTER));
```

❌ **错误做法（之前的问题）：**
```java
// 空格分隔，Spring Boot无法解析
.format("yyyy-MM-dd HH:mm:ss")
```

**原因：**
- Spring Boot的Jackson默认期望ISO 8601格式
- 日期和时间之间必须用`T`分隔，不能用空格
- 这是国际标准格式，前后端都能正确解析

---

### 2. 数据格式匹配

**前端期望的数据结构：**
```javascript
{
  student: {
    name: '张三',
    studentNo: '20240001',
    class: '生物技术1班',
    phone: '13800138000',
    email: 'zhangsan@example.com',
    accessStatus: 'normal',  // ← 字符串，不是数字
    accessExpire: '2026-12-31'
  },
  violations: [...]
}
```

**后端实现：**
```java
Map<String, Object> studentInfo = new HashMap<>();
studentInfo.put("accessStatus", student.getAccessStatus() == 1 ? "normal" : "disabled");
// ↑ 将数据库的整数（1/2）转换为前端期望的字符串（normal/disabled）
```

---

### 3. 关联查询优化

**违规记录需要显示设备名称和处理老师姓名：**

```
violation.borrow_id → borrow_record.device_id → device.name
violation.teacher_id → teacher.name
```

**实现方式：**
```java
private String getDeviceNameByBorrowId(Integer borrowId) {
    if (borrowId == null) return "未知设备";
    
    BorrowRecord borrowRecord = borrowRecordMapper.selectById(borrowId);
    if (borrowRecord == null || borrowRecord.getDeviceId() == null) {
        return "未知设备";
    }
    
    Device device = deviceMapper.selectById(borrowRecord.getDeviceId());
    return device != null ? device.getName() : "未知设备";
}
```

**性能优化建议：**
- 当前实现是N+1查询（每条违规记录查询2次）
- 如果违规记录较多，建议使用JOIN查询或批量查询
- 可以添加缓存（Redis）减少数据库压力

---

### 4. 只允许更新特定字段

**安全性考虑：**
```java
public boolean updateStudentProfile(Integer studentId, Map<String, String> updates) {
    Student student = studentMapper.selectById(studentId);
    
    // 只允许更新phone和email
    if (updates.containsKey("phone")) {
        student.setPhone(updates.get("phone"));
    }
    if (updates.containsKey("email")) {
        student.setEmail(updates.get("email"));
    }
    
    int rows = studentMapper.updateById(student);
    return rows > 0;
}
```

**防止恶意修改：**
- ❌ 不允许修改：name, studentNo, className, accessStatus等
- ✅ 允许修改：phone, email（用户可自行维护的信息）

---

## 🧪 测试指南

### 1. 启动后端服务

```bash
cd backed
mvn spring-boot:run
```

确保服务运行在 http://localhost:8080

---

### 2. 测试获取个人资料

**浏览器访问：**
```
http://localhost:8080/api/v1/student/profile
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "student": {
      "name": "张三",
      "studentNo": "20240001",
      "class": "生物技术1班",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "accessStatus": "normal",
      "accessExpire": "2026-12-31"
    },
    "violations": []
  },
  "timestamp": 1705315200000
}
```

---

### 3. 测试更新个人资料

**使用curl测试：**
```bash
curl -X PUT http://localhost:8080/api/v1/student/profile \
  -H "Content-Type: application/json" \
  -d '{"phone":"13900139000","email":"newemail@example.com"}'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1705315200000
}
```

---

### 4. 前端测试

1. **启动前端服务：**
   ```bash
   cd frontend
   npm run dev
   ```

2. **访问个人中心页面：**
   ```
   http://localhost:3000/student/profile
   ```

3. **验证功能：**
   - ✅ 个人信息正确显示
   - ✅ 违规记录列表正确显示（如果有）
   - ✅ 点击"编辑"按钮可以修改电话/邮箱
   - ✅ 保存后数据立即更新

---

## 📊 数据库依赖

### 需要的表

1. **student** - 学生表
   - 必须有数据才能查询个人资料

2. **violation** - 违规记录表
   - 可以有0条或多条记录
   - 通过student_id关联

3. **borrow_record** - 借用记录表
   - 用于查询违规记录对应的设备名称
   - 通过violation.borrow_id关联

4. **device** - 设备表
   - 用于获取设备名称
   - 通过borrow_record.device_id关联

5. **teacher** - 老师表
   - 用于获取处理老师姓名
   - 通过violation.teacher_id关联

---

## ⚠️ 注意事项

### 1. 学生ID硬编码问题

**当前实现：**
```java
Integer studentId = 1;  // TODO: 从Token中获取学生ID
```

**后续改进：**
- 实现JWT认证中间件
- 从请求头`Authorization: Bearer {token}`中解析学生ID
- 添加权限验证（确保只能查询自己的资料）

---

### 2. 空值处理

**代码中的空值检查：**
```java
if (borrowId == null) return "未知设备";
if (borrowRecord == null) return "未知设备";
if (device == null) return "未知设备";
```

**原因：**
- 数据库中可能存在脏数据
- 关联的记录可能已被删除
- 避免NullPointerException

---

### 3. 时间格式一致性

**所有时间字段统一使用ISO 8601格式：**
- ✅ `yyyy-MM-dd'T'HH:mm:ss` - 日期时间
- ✅ `yyyy-MM-dd` - 纯日期

**不要混用：**
- ❌ `yyyy-MM-dd HH:mm:ss` - 空格分隔（会导致解析错误）

---

## 🎯 完成状态

| 功能模块 | 状态 | 说明 |
|---------|------|------|
| 获取学生信息 | ✅ 完成 | 包含所有必需字段 |
| 获取违规记录 | ✅ 完成 | 包含设备名称和老师姓名 |
| 更新电话 | ✅ 完成 | 支持单独更新 |
| 更新邮箱 | ✅ 完成 | 支持单独更新 |
| 时间格式 | ✅ 正确 | 使用ISO 8601标准 |
| 数据格式匹配 | ✅ 正确 | 与前端期望完全一致 |
| 空值处理 | ✅ 完善 | 避免NullPointerException |
| 权限控制 | ⚠️ 待完善 | 当前使用硬编码studentId |

---

## 🚀 下一步优化建议

1. **实现JWT认证**
   - 从Token中获取真实的学生ID
   - 添加权限拦截器

2. **性能优化**
   - 使用JOIN查询减少数据库访问次数
   - 添加Redis缓存学生资料

3. **数据验证**
   - 添加手机号格式验证
   - 添加邮箱格式验证

4. **操作日志**
   - 记录资料修改历史
   - 记录IP地址和操作时间

5. **通知功能**
   - 资料修改成功后发送通知
   - 违规记录新增时推送消息

---

## 📝 总结

✅ **已完成：**
- 创建了完整的后端架构（Entity、Mapper、Service、Controller）
- 实现了获取和更新个人资料的API
- 时间格式统一使用ISO 8601标准（避免之前的错误）
- 数据格式与前端完全匹配
- 添加了完善的空值处理和异常处理

⚠️ **待完善：**
- JWT认证（从Token获取学生ID）
- 性能优化（减少N+1查询）
- 数据验证（手机号、邮箱格式）

🎉 **现在可以重启后端并刷新浏览器测试个人中心页面了！**
