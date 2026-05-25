# 管理端"管理仪表盘"功能实现文档

## 📋 功能概述

完成了老师端管理仪表盘的后端实现，提供全面的实验室运营数据概览，包括：
- ✅ 9个关键统计指标
- ✅ 智能待办事项列表（按优先级排序）
- ✅ TOP5设备借用排行（柱状图）
- ✅ 月度借用趋势分析（折线图）
- ✅ 实时数据计算，无缓存延迟

---

## 🏗️ 后端架构

### 创建的文件清单

#### 1. TeacherDashboardService.java - 服务接口
**路径**：`backed/src/main/java/com/lab/backed/service/TeacherDashboardService.java`

**方法**：
```java
Map<String, Object> getDashboardData();
```

---

#### 2. TeacherDashboardServiceImpl.java - 服务实现
**路径**：`backed/src/main/java/com/lab/backed/service/impl/TeacherDashboardServiceImpl.java`

**核心方法**：
- `getDashboardData()` - 主方法，返回完整仪表盘数据
- `getStats()` - 计算9个统计指标
- `getTodos()` - 生成待办事项列表
- `getCharts()` - 准备图表数据

**依赖的Mapper**：
- DeviceMapper - 设备数据
- ReservationMapper - 预约数据
- BorrowRecordMapper - 借用记录数据
- StudentMapper - 学生数据
- ViolationMapper - 违规记录数据

---

#### 3. TeacherDashboardController.java - 控制器
**路径**：`backed/src/main/java/com/lab/backed/controller/TeacherDashboardController.java`

**接口**：
```
GET /api/v1/teacher/dashboard
```

---

## 📊 API接口说明

### 获取仪表盘数据

**接口地址**：`GET /api/v1/teacher/dashboard`

**请求参数**：无

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stats": {
      "deviceCount": 156,
      "availableCount": 120,
      "repairCount": 5,
      "todayReservationCount": 8,
      "pendingAuditCount": 3,
      "borrowedCount": 25,
      "overdueCount": 2,
      "activeStudentCount": 45,
      "violationStudentCount": 8
    },
    "todos": [
      {
        "id": 1,
        "type": "overdue",
        "deviceName": "显微镜",
        "studentName": "张三",
        "time": "2天",
        "priority": "high"
      }
    ],
    "charts": {
      "topDevices": [
        { "name": "显微镜", "count": 45 },
        { "name": "离心机", "count": 32 }
      ],
      "monthlyTrend": [
        { "month": "2025-12", "count": 120 },
        { "month": "2026-01", "count": 85 }
      ]
    }
  }
}
```

---

## 🎯 核心功能详解

### 1. 统计数据（9个指标）

#### 1.1 设备总数 (deviceCount)
```sql
SELECT COUNT(*) FROM device;
```

#### 1.2 可借用数 (availableCount)
```sql
SELECT COUNT(*) FROM device WHERE status = 'available';
```

#### 1.3 维修中数 (repairCount)
```sql
SELECT COUNT(*) FROM device WHERE status = 'repair';
```

#### 1.4 今日预约数 (todayReservationCount)
```sql
SELECT COUNT(*) FROM reservation 
WHERE start_time >= TODAY 00:00:00 
  AND start_time < TOMORROW 00:00:00;
```

**实现逻辑**：
```java
LocalDate today = LocalDate.now();
LocalDateTime startOfDay = today.atStartOfDay();
LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
wrapper.ge(Reservation::getStartTime, startOfDay)
       .lt(Reservation::getStartTime, endOfDay);
```

#### 1.5 待审核数 (pendingAuditCount)
```sql
SELECT COUNT(*) FROM reservation WHERE status = 'pending';
```

#### 1.6 借用中数 (borrowedCount)
```sql
SELECT COUNT(*) FROM borrow_record 
WHERE status IN ('borrowed', 'overdue');
```

**说明**：包括正常借用和超时借用两种状态。

#### 1.7 超时数 (overdueCount)
```sql
SELECT COUNT(*) FROM borrow_record WHERE status = 'overdue';
```

#### 1.8 活跃学生数 (activeStudentCount)
```sql
SELECT COUNT(DISTINCT student_id) FROM borrow_record 
WHERE borrow_time >= NOW() - INTERVAL 30 DAY;
```

**定义**：最近30天内有借用记录的学生数量。

**实现逻辑**：
```java
LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
wrapper.ge(BorrowRecord::getBorrowTime, thirtyDaysAgo)
       .select(BorrowRecord::getStudentId)
       .groupBy(BorrowRecord::getStudentId);
List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);
long count = records.stream()
    .map(BorrowRecord::getStudentId)
    .distinct()
    .count();
```

#### 1.9 违规学生数 (violationStudentCount)
```sql
SELECT COUNT(DISTINCT student_id) FROM violation 
WHERE status = 1;
```

**定义**：有有效违规记录（未撤销）的学生数量。

---

### 2. 待办事项（智能排序）

待办事项分为三类，按优先级自动排序：

#### 2.1 超时未还（高优先级 - high）

**数据来源**：`borrow_record` 表中 `status = 'overdue'` 的记录

**显示内容**：
- 设备名称
- 学生姓名
- 超时天数
- 优先级：high

**查询逻辑**：
```java
LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(BorrowRecord::getStatus, "overdue")
       .orderByDesc(BorrowRecord::getDueTime)
       .last("LIMIT 3");  // 最多显示3条
```

**超时天数计算**：
```java
long overdueDays = Duration.between(record.getDueTime(), LocalDateTime.now()).toDays();
```

---

#### 2.2 待审核预约（中优先级 - medium）

**数据来源**：`reservation` 表中 `status = 'pending'` 的记录

**显示内容**：
- 设备名称
- 学生姓名
- 等待时长（小时）
- 优先级：medium

**查询逻辑**：
```java
LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Reservation::getStatus, "pending")
       .orderByAsc(Reservation::getCreatedAt)  // 最早的优先
       .last("LIMIT 3");  // 最多显示3条
```

**等待时长计算**：
```java
long waitingHours = Duration.between(reservation.getCreatedAt(), LocalDateTime.now()).toHours();
```

---

#### 2.3 排序规则

待办事项按优先级排序：**high > medium > low**

```java
todos.sort((a, b) -> {
    String priorityA = (String) a.get("priority");
    String priorityB = (String) b.get("priority");
    if ("high".equals(priorityA)) return -1;
    if ("high".equals(priorityB)) return 1;
    if ("medium".equals(priorityA)) return -1;
    if ("medium".equals(priorityB)) return 1;
    return 0;
});
```

---

### 3. 图表数据

#### 3.1 TOP5设备借用排行（柱状图）

**时间范围**：最近30天

**数据来源**：`borrow_record` 表

**查询逻辑**：
```java
// 1. 查询最近30天的借用记录
LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
wrapper.ge(BorrowRecord::getBorrowTime, thirtyDaysAgo);
List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);

// 2. 按设备ID分组统计
Map<Integer, Long> deviceBorrowCount = records.stream()
    .collect(Collectors.groupingBy(BorrowRecord::getDeviceId, Collectors.counting()));

// 3. 取TOP5
List<Map<String, Object>> topDevices = deviceBorrowCount.entrySet().stream()
    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
    .limit(5)
    .map(entry -> {
        Device device = deviceMapper.selectById(entry.getKey());
        Map<String, Object> map = new HashMap<>();
        map.put("name", device.getName());
        map.put("count", entry.getValue());
        return map;
    })
    .collect(Collectors.toList());
```

**前端展示**：ECharts柱状图

---

#### 3.2 月度借用趋势（折线图）

**时间范围**：最近6个月（包含当前月）

**数据来源**：`borrow_record` 表

**查询逻辑**：
```java
List<Map<String, Object>> monthlyTrend = new ArrayList<>();
LocalDate now = LocalDate.now();

for (int i = 5; i >= 0; i--) {
    // 计算每个月的起止时间
    LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
    LocalDate monthEnd = monthStart.plusMonths(1);
    
    LocalDateTime monthStartDT = monthStart.atStartOfDay();
    LocalDateTime monthEndDT = monthEnd.atStartOfDay();
    
    // 查询该月的借用记录数
    LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
    wrapper.ge(BorrowRecord::getBorrowTime, monthStartDT)
           .lt(BorrowRecord::getBorrowTime, monthEndDT);
    long count = borrowRecordMapper.selectCount(wrapper);
    
    // 添加到结果列表
    Map<String, Object> monthData = new HashMap<>();
    monthData.put("month", monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM")));
    monthData.put("count", count);
    monthlyTrend.add(monthData);
}
```

**示例输出**：
```json
[
  { "month": "2025-08", "count": 95 },
  { "month": "2025-09", "count": 110 },
  { "month": "2025-10", "count": 88 },
  { "month": "2025-11", "count": 102 },
  { "month": "2025-12", "count": 120 },
  { "month": "2026-01", "count": 85 }
]
```

**前端展示**：ECharts折线图（带面积填充）

---

## 🔍 技术要点

### 1. 时间计算

#### 使用Java 8 Time API
```java
// 获取今天零点
LocalDate today = LocalDate.now();
LocalDateTime startOfDay = today.atStartOfDay();

// 获取明天零点
LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

// 计算时间差
long days = Duration.between(startTime, endTime).toDays();
long hours = Duration.between(startTime, endTime).toHours();
```

---

### 2. Stream流式处理

#### 分组统计
```java
Map<Integer, Long> deviceBorrowCount = records.stream()
    .collect(Collectors.groupingBy(
        BorrowRecord::getDeviceId,  // 按设备ID分组
        Collectors.counting()        // 计数
    ));
```

#### 排序并取TOP N
```java
List<Map<String, Object>> top5 = deviceBorrowCount.entrySet().stream()
    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())  // 降序
    .limit(5)  // 取前5个
    .map(entry -> { ... })  // 转换为Map
    .collect(Collectors.toList());
```

#### 去重计数
```java
long distinctCount = records.stream()
    .map(BorrowRecord::getStudentId)
    .distinct()
    .count();
```

---

### 3. MyBatis-Plus查询优化

#### 只查询需要的字段
```java
LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
wrapper.ge(BorrowRecord::getBorrowTime, thirtyDaysAgo)
       .select(BorrowRecord::getStudentId)  // 只查student_id
       .groupBy(BorrowRecord::getStudentId);
```

#### 限制返回数量
```java
wrapper.last("LIMIT 3");  // SQL层面限制
```

---

### 4. 关联查询

#### 获取设备名称和学生姓名
```java
// 查询借用记录
BorrowRecord record = borrowRecordMapper.selectById(borrowId);

// 关联查询设备
Device device = deviceMapper.selectById(record.getDeviceId());
String deviceName = device != null ? device.getName() : "未知设备";

// 关联查询学生
Student student = studentMapper.selectById(record.getStudentId());
String studentName = student != null ? student.getName() : "未知学生";
```

**注意**：这里使用了多次单表查询而非JOIN，因为：
- ✅ 代码更清晰，易于维护
- ✅ MyBatis-Plus的单表查询性能优秀
- ✅ 避免复杂的JOIN语句
- ❌ 如果数据量大，可以考虑使用JOIN或缓存优化

---

## 🧪 测试步骤

### 1. 启动后端服务

```bash
cd backed
mvn clean package
java -jar target/backed-0.0.1-SNAPSHOT.jar
```

---

### 2. 访问仪表盘页面

浏览器访问：http://localhost:3000/teacher/dashboard

---

### 3. 验证统计数据

检查页面上显示的9个统计卡片：
- ✅ 设备总数
- ✅ 可借用数
- ✅ 今日预约
- ✅ 待审核数
- ✅ 借用中数
- ✅ 超时数
- ✅ 活跃学生
- ✅ 违规学生

---

### 4. 验证待办事项

检查待办事项列表：
- ✅ 显示超时未还记录（红色标签，高优先级）
- ✅ 显示待审核预约（橙色标签，中优先级）
- ✅ 按优先级排序
- ✅ 显示设备名、学生名、时长

---

### 5. 验证图表

#### TOP5设备柱状图
- ✅ 显示借用次数最多的5个设备
- ✅ 柱状图高度与借用次数成正比
- ✅ 鼠标悬停显示具体数值

#### 月度趋势折线图
- ✅ 显示最近6个月的数据
- ✅ 折线平滑过渡
- ✅ 带面积填充效果
- ✅ X轴显示月份（yyyy-MM格式）

---

### 6. API测试

使用curl或Postman测试API：

```bash
curl -X GET "http://localhost:8080/api/v1/teacher/dashboard" \
  -H "Authorization: Bearer YOUR_TEACHER_TOKEN"
```

**预期响应**：
- code: 200
- data.stats: 包含9个统计字段
- data.todos: 待办事项数组（最多6条）
- data.charts.topDevices: TOP5设备数组
- data.charts.monthlyTrend: 6个月趋势数组

---

## 📈 性能优化建议

### 1. 添加缓存

对于不频繁变化的数据，可以添加Redis缓存：

```java
@Cacheable(value = "teacher:dashboard", key = "'stats'", unless = "#result == null")
private Map<String, Object> getStats() {
    // ...
}
```

**缓存策略**：
- stats: 缓存5分钟
- todos: 缓存1分钟
- charts: 缓存10分钟

---

### 2. 异步加载

将耗时的统计查询改为异步执行：

```java
@Async
public CompletableFuture<Map<String, Object>> getStatsAsync() {
    return CompletableFuture.completedFuture(getStats());
}

@Async
public CompletableFuture<List<Map<String, Object>>> getTodosAsync() {
    return CompletableFuture.completedFuture(getTodos());
}

@Async
public CompletableFuture<Map<String, Object>> getChartsAsync() {
    return CompletableFuture.completedFuture(getCharts());
}
```

---

### 3. 数据库索引优化

确保以下字段有索引：
- `borrow_record.borrow_time` - 用于时间范围查询
- `borrow_record.status` - 用于状态筛选
- `reservation.status` - 用于待审核查询
- `reservation.start_time` - 用于今日预约查询
- `violation.status` - 用于有效违规查询

---

## 🎨 前端对接说明

### 数据映射

后端返回的数据结构与前端期望完全匹配：

| 后端字段 | 前端使用 | 说明 |
|---------|---------|------|
| stats.deviceCount | 统计卡片1 | 设备总数 |
| stats.availableCount | 统计卡片2 | 可借用数 |
| stats.todayReservationCount | 统计卡片3 | 今日预约 |
| stats.pendingAuditCount | 统计卡片4 | 待审核数 |
| stats.borrowedCount | 统计卡片5 | 借用中数 |
| stats.overdueCount | 统计卡片6 | 超时数 |
| stats.activeStudentCount | 统计卡片7 | 活跃学生 |
| stats.violationStudentCount | 统计卡片8 | 违规学生 |
| todos[] | 待办事项列表 | 最多6条 |
| charts.topDevices[] | TOP5柱状图 | ECharts数据源 |
| charts.monthlyTrend[] | 月度趋势折线图 | ECharts数据源 |

---

### 禁用Mock

修改前端API配置，切换到真实后端：

**文件**：`frontend/src/api/teacher/index.js`

```javascript
const USE_MOCK = false  // 改为false
```

---

## ✅ 完成检查清单

- [x] 创建TeacherDashboardService接口
- [x] 实现getStats()方法（9个统计指标）
- [x] 实现getTodos()方法（智能待办列表）
- [x] 实现getCharts()方法（图表数据）
- [x] 创建TeacherDashboardController
- [x] 实现GET /api/v1/teacher/dashboard接口
- [x] 数据结构与前端期望完全匹配
- [x] 时间计算使用Java 8 Time API
- [x] 使用Stream进行数据处理
- [x] 待办事项按优先级排序
- [x] TOP5设备按借用次数降序
- [x] 月度趋势显示最近6个月
- [x] 关联查询设备名称和学生姓名
- [x] 处理空值情况（未知设备/学生）

---

## 🚀 后续扩展方向

### 1. 更多统计维度

- 设备类别占比（饼图）
- 学生班级活跃度排行
- 违规类型分布
- 设备使用率热力图

### 2. 实时推送

使用WebSocket推送实时数据更新：
- 新预约申请
- 设备归还
- 超时提醒

### 3. 自定义时间范围

允许老师选择统计时间范围：
- 今天/本周/本月/本学期
- 自定义日期范围

### 4. 导出报表

支持导出仪表盘数据为Excel/PDF：
- 月度运营报告
- 学期总结报告
- 年度分析报告

---

## 📝 总结

本次实现完成了管理端仪表盘的核心功能：

✅ **数据统计**：9个关键指标，实时计算  
✅ **待办事项**：智能排序，优先级分明  
✅ **图表分析**：TOP5设备 + 月度趋势  
✅ **前后端对接**：数据结构完全匹配  
✅ **代码质量**：清晰的分层架构，易于维护  

现在老师可以一目了然地掌握实验室的运营状况，快速处理待办事项，并通过图表分析设备使用情况！🎉

---

**实现日期**：2026-05-19  
**实现人员**：AI Assistant  
**影响范围**：老师端管理仪表盘  
**依赖模块**：Device, Reservation, BorrowRecord, Student, Violation
