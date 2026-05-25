# 学生端"首页"功能实现文档

## 📋 功能概述

完成了学生端"首页/仪表盘"页面的后端实现，包括：
- ✅ 获取学生基本信息（姓名、学号、实验室类型、准入有效期）
- ✅ 统计当前借用设备数
- ✅ 统计待处理预约数
- ✅ 获取最新3条公告（根据实验室类型筛选）
- ✅ 时间格式统一使用 ISO 8601 标准
- ✅ 数据格式与前端完全匹配

---

## 🏗️ 后端架构

### 创建的文件清单

#### 1. 实体类（Entity）
- **Announcement.java** - 公告实体
  - 路径：`backed/src/main/java/com/lab/backed/entity/Announcement.java`
  - 字段：id, title, content, attachments, targetType, targetIds, isPinned, publishTime, teacherId, status, createdAt

#### 2. Mapper接口
- **AnnouncementMapper.java** - 公告数据访问层
  - 路径：`backed/src/main/java/com/lab/backed/mapper/AnnouncementMapper.java`
  - 继承：`BaseMapper<Announcement>`

#### 3. Service层
- **DashboardService.java** - 首页服务接口
  - 路径：`backed/src/main/java/com/lab/backed/service/DashboardService.java`
  - 方法：`getStudentDashboard(Integer studentId)` - 获取首页数据

- **DashboardServiceImpl.java** - 首页服务实现
  - 路径：`backed/src/main/java/com/lab/backed/service/impl/DashboardServiceImpl.java`
  - 核心功能：
    - 查询学生基本信息
    - 统计当前借用设备数（borrowed/overdue状态）
    - 统计待处理预约数（pending状态）
    - 查询最新3条公告（按实验室类型筛选）
    - 时间格式化（ISO 8601标准）

#### 4. Controller层
- **StudentDashboardController.java** - 学生首页控制器
  - 路径：`backed/src/main/java/com/lab/backed/controller/StudentDashboardController.java`
  - 接口：`GET /api/v1/student/dashboard` - 获取首页数据

---

## 📡 API接口文档

### 获取首页数据

**接口地址：** `GET /api/v1/student/dashboard`

**请求参数：** 无（学生ID从Token中获取，当前使用模拟数据studentId=1）

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "student": {
      "id": 1,
      "name": "张三",
      "studentNo": "20240001",
      "labType": "bio",
      "accessExpire": "2026-12-31"
    },
    "stats": {
      "currentBorrowCount": 2,
      "pendingReservationCount": 1
    },
    "announcements": [
      {
        "id": 1,
        "title": "实验室开放时间调整通知",
        "publishTime": "2026-01-10T09:00:00",
        "isRead": false
      },
      {
        "id": 2,
        "title": "新设备投入使用",
        "publishTime": "2026-01-08T14:30:00",
        "isRead": false
      }
    ]
  },
  "timestamp": 1705315200000
}
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| student.id | Integer | 学生ID |
| student.name | String | 学生姓名 |
| student.studentNo | String | 学号 |
| student.labType | String | 实验室类型：bio/chem |
| student.accessExpire | String | 准入有效期（格式：yyyy-MM-dd） |
| stats.currentBorrowCount | Long | 当前借用设备数 |
| stats.pendingReservationCount | Long | 待处理预约数 |
| announcements | Array | 公告列表（最多3条） |
| announcements[].id | Integer | 公告ID |
| announcements[].title | String | 公告标题 |
| announcements[].publishTime | String | 发布时间（格式：yyyy-MM-dd'T'HH:mm:ss） |
| announcements[].isRead | Boolean | 是否已读 |

---

## 🔧 技术要点

### 1. 时间格式规范

✅ **正确做法：**
```java
// 日期格式（用于accessExpire）
private static final DateTimeFormatter DATE_FORMATTER = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd");

// 日期时间格式（用于publishTime）
private static final DateTimeFormatter DATETIME_FORMATTER = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

// 使用示例
aMap.put("publishTime", a.getPublishTime().format(DATETIME_FORMATTER));
```

**原因：**
- Spring Boot的Jackson默认期望ISO 8601格式
- 日期和时间之间必须用`T`分隔
- 这是国际标准格式，前后端都能正确解析

---

### 2. 统计数据计算

**当前借用设备数：**
```java
LambdaQueryWrapper<BorrowRecord> borrowWrapper = new LambdaQueryWrapper<>();
borrowWrapper.eq(BorrowRecord::getStudentId, studentId)
             .in(BorrowRecord::getStatus, "borrowed", "overdue");
long currentBorrowCount = borrowRecordMapper.selectCount(borrowWrapper);
```

**说明：**
- 统计状态为 `borrowed`（借用中）或 `overdue`（已超时）的记录
- 不包括 `returned`（已归还）的记录

---

**待处理预约数：**
```java
LambdaQueryWrapper<Reservation> reservationWrapper = new LambdaQueryWrapper<>();
reservationWrapper.eq(Reservation::getStudentId, studentId)
                  .eq(Reservation::getStatus, "pending");
long pendingReservationCount = reservationMapper.selectCount(reservationWrapper);
```

**说明：**
- 只统计状态为 `pending`（待审核）的预约
- 不包括 `approved`（已通过）、`rejected`（已拒绝）、`cancelled`（已取消）的预约

---

### 3. 公告筛选逻辑

**根据实验室类型筛选：**
```java
LambdaQueryWrapper<Announcement> announcementWrapper = new LambdaQueryWrapper<>();
announcementWrapper.eq(Announcement::getStatus, 1)  // 正常状态
                  .and(w -> w.eq(Announcement::getTargetType, "all")  // 全部
                           .or()
                           .eq(Announcement::getTargetType, student.getLabType()))  // 对应实验室
                  .orderByDesc(Announcement::getIsPinned)  // 置顶优先
                  .orderByDesc(Announcement::getPublishTime)  // 时间倒序
                  .last("LIMIT 3");  // 只取3条
```

**筛选规则：**
1. 只查询状态正常的公告（status = 1）
2. 公告的目标类型必须是：
   - `all`（所有实验室），或
   - 与学生所属实验室类型匹配（如 `bio` 或 `chem`）
3. 置顶公告优先显示（is_pinned 降序）
4. 按发布时间倒序排列
5. 只返回最新的3条

---

### 4. 数据格式匹配

**前端期望的数据结构：**
```javascript
{
  student: {
    id: 1,
    name: '张三',
    studentNo: '20240001',
    labType: 'bio',
    accessExpire: '2026-12-31'
  },
  stats: {
    currentBorrowCount: 2,
    pendingReservationCount: 1
  },
  announcements: [...]
}
```

**后端实现：**
```java
Map<String, Object> result = new HashMap<>();
result.put("student", studentInfo);
result.put("stats", stats);
result.put("announcements", announcementList);
```

**完全匹配，无需前端转换！**

---

## 🧪 测试指南

### 1. 启动后端服务

```bash
cd backed
mvn spring-boot:run
```

确保服务运行在 http://localhost:8080

---

### 2. 准备测试数据

**确保数据库中有以下数据：**

1. **学生记录**（student表）
   ```sql
   INSERT INTO student (student_no, name, class_name, phone, email, password, lab_type, access_status, access_expire, violation_count, status)
   VALUES ('20240001', '张三', '生物技术1班', '13800138000', 'zhangsan@example.com', '$2a$10$...', 'bio', 1, '2026-12-31', 0, 1);
   ```

2. **借用记录**（borrow_record表）
   ```sql
   -- 当前借用的设备
   INSERT INTO borrow_record (student_id, device_id, borrow_time, due_time, status, is_overdue)
   VALUES (1, 1, '2026-01-10 09:00:00', '2026-01-13 09:00:00', 'borrowed', 0);
   
   INSERT INTO borrow_record (student_id, device_id, borrow_time, due_time, status, is_overdue)
   VALUES (1, 2, '2026-01-11 09:00:00', '2026-01-14 09:00:00', 'borrowed', 0);
   ```

3. **预约记录**（reservation表）
   ```sql
   -- 待审核的预约
   INSERT INTO reservation (student_id, device_id, start_time, end_time, purpose, status)
   VALUES (1, 3, '2026-01-20 08:00:00', '2026-01-20 12:00:00', '细胞观察实验', 'pending');
   ```

4. **公告记录**（announcement表）
   ```sql
   -- 生物实验室公告
   INSERT INTO announcement (title, content, target_type, is_pinned, publish_time, teacher_id, status)
   VALUES ('实验室开放时间调整通知', '自2026年1月起，实验室开放时间调整为...', 'bio', 1, '2026-01-10 09:00:00', 1, 1);
   
   INSERT INTO announcement (title, content, target_type, is_pinned, publish_time, teacher_id, status)
   VALUES ('新设备投入使用', '最近采购了一批新设备，欢迎大家使用...', 'bio', 0, '2026-01-08 14:30:00', 1, 1);
   
   -- 全局公告
   INSERT INTO announcement (title, content, target_type, is_pinned, publish_time, teacher_id, status)
   VALUES ('寒假安排通知', '寒假期间实验室将关闭...', 'all', 0, '2026-01-05 10:00:00', 1, 1);
   ```

---

### 3. 测试API

**浏览器访问：**
```
http://localhost:8080/api/v1/student/dashboard
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "student": {
      "id": 1,
      "name": "张三",
      "studentNo": "20240001",
      "labType": "bio",
      "accessExpire": "2026-12-31"
    },
    "stats": {
      "currentBorrowCount": 2,
      "pendingReservationCount": 1
    },
    "announcements": [
      {
        "id": 1,
        "title": "实验室开放时间调整通知",
        "publishTime": "2026-01-10T09:00:00",
        "isRead": false
      },
      {
        "id": 2,
        "title": "新设备投入使用",
        "publishTime": "2026-01-08T14:30:00",
        "isRead": false
      },
      {
        "id": 3,
        "title": "寒假安排通知",
        "publishTime": "2026-01-05T10:00:00",
        "isRead": false
      }
    ]
  },
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

2. **访问首页：**
   ```
   http://localhost:3000/student/dashboard
   ```

3. **验证功能：**
   - ✅ 欢迎横幅显示学生姓名、学号、实验室类型
   - ✅ 统计卡片显示正确的借用数和预约数
   - ✅ 公告栏显示最新3条公告
   - ✅ 快速入口按钮可以正常跳转

---

## 📊 数据库依赖

### 需要的表

1. **student** - 学生表
   - 必须有数据才能查询学生信息

2. **borrow_record** - 借用记录表
   - 用于统计当前借用设备数
   - 通过 student_id 关联

3. **reservation** - 预约表
   - 用于统计待处理预约数
   - 通过 student_id 关联

4. **announcement** - 公告表
   - 用于获取最新公告
   - 通过 target_type 筛选

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
- 添加权限验证

---

### 2. 公告已读状态

**当前实现：**
```java
aMap.put("isRead", false);  // TODO: 实际应该从通知表查询已读状态
```

**后续改进：**
- 创建 notification 表记录用户的已读状态
- 查询公告时关联查询 notification 表
- 根据 user_id 和 announcement_id 判断是否已读

---

### 3. 公告筛选逻辑

**当前筛选规则：**
- 只显示 `target_type = 'all'` 或 `target_type = student.lab_type` 的公告

**可能的扩展：**
- 支持按班级筛选（target_type = 'class'，target_ids 包含班级ID列表）
- 支持定向发送给学生（target_type = 'student'，target_ids 包含学生ID列表）

---

### 4. 性能优化

**当前实现：**
- 3次独立查询（学生信息、借用统计、预约统计、公告列表）

**优化建议：**
- 如果数据量大，可以考虑缓存（Redis）
- 统计数据可以定时更新，避免每次实时计算
- 公告列表可以添加缓存，减少数据库查询

---

## 🎯 完成状态

| 功能模块 | 状态 | 说明 |
|---------|------|------|
| 学生信息查询 | ✅ 完成 | 包含所有必需字段 |
| 借用设备统计 | ✅ 完成 | 统计borrowed/overdue状态 |
| 预约数量统计 | ✅ 完成 | 统计pending状态 |
| 公告列表查询 | ✅ 完成 | 按实验室类型筛选，最多3条 |
| 时间格式 | ✅ 正确 | 使用ISO 8601标准 |
| 数据格式匹配 | ✅ 正确 | 与前端期望完全一致 |
| 置顶公告优先 | ✅ 完成 | is_pinned降序排列 |
| 权限控制 | ⚠️ 待完善 | 当前使用硬编码studentId |
| 公告已读状态 | ⚠️ 待完善 | 当前固定为false |

---

## 🚀 下一步优化建议

1. **实现JWT认证**
   - 从Token中获取真实的学生ID
   - 添加权限拦截器

2. **完善公告已读状态**
   - 创建 notification 表
   - 记录每个用户对每条公告的已读状态

3. **性能优化**
   - 添加Redis缓存统计数据
   - 缓存公告列表（5分钟过期）

4. **扩展公告筛选**
   - 支持按班级筛选
   - 支持定向发送给学生

5. **添加更多统计维度**
   - 累计借用次数
   - 违规次数
   - 本月借用次数

---

## 📝 总结

✅ **已完成：**
- 创建了完整的后端架构（Entity、Mapper、Service、Controller）
- 实现了获取首页数据的API
- 时间格式统一使用ISO 8601标准
- 数据格式与前端完全匹配
- 公告按实验室类型智能筛选
- 置顶公告优先显示

⚠️ **待完善：**
- JWT认证（从Token获取学生ID）
- 公告已读状态（需要notification表）
- 性能优化（添加缓存）

🎉 **现在可以重启后端并刷新浏览器测试首页了！**
