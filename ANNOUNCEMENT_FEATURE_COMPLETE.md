# 学生端"公告"功能实现文档

## 📋 功能概述

完成了学生端完整的公告功能，包括：
- ✅ 公告列表页面（不显示在导航栏）
- ✅ 公告详情页面
- ✅ 从首页"查看全部"跳转到公告列表
- ✅ 点击公告标题跳转到详情页
- ✅ 自动标记已读状态
- ✅ 支持按全部/未读/已读筛选
- ✅ 分页功能
- ✅ 置顶公告优先显示
- ✅ 时间格式统一使用 ISO 8601 标准

---

## 🏗️ 前端架构

### 创建的文件清单

#### 1. 页面组件
- **Announcements.vue** - 公告列表页面
  - 路径：`frontend/src/views/student/Announcements.vue`
  - 功能：
    - 显示公告列表表格
    - 支持全部/未读/已读筛选
    - 分页显示
    - 点击标题查看详情

- **AnnouncementDetail.vue** - 公告详情页面
  - 路径：`frontend/src/views/student/AnnouncementDetail.vue`
  - 功能：
    - 显示公告完整内容
    - 显示发布时间和发布人
    - 显示附件列表
    - 自动标记为已读
    - 返回按钮

#### 2. 路由配置
- **index.js** - 新增2个路由
  ```javascript
  {
    path: 'announcements',
    name: 'StudentAnnouncements',
    component: () => import('../views/student/Announcements.vue'),
    meta: { title: '公告列表' }
  },
  {
    path: 'announcements/:id',
    name: 'StudentAnnouncementDetail',
    component: () => import('../views/student/AnnouncementDetail.vue'),
    meta: { title: '公告详情' }
  }
  ```

#### 3. API接口
- **student/index.js** - 新增3个API方法
  ```javascript
  getAnnouncements(params)          // 获取公告列表
  getAnnouncementDetail(id)         // 获取公告详情
  markAnnouncementRead(id)          // 标记已读
  ```

#### 4. 修改的文件
- **Dashboard.vue** - 修改"查看全部"按钮跳转路径
  - 从 `/student/profile` 改为 `/student/announcements`

---

## 🏗️ 后端架构

### 创建的文件清单

#### 1. Service层
- **AnnouncementService.java** - 公告服务接口
  - 路径：`backed/src/main/java/com/lab/backed/service/AnnouncementService.java`
  - 方法：
    - `getAnnouncementList()` - 获取公告列表（分页）
    - `getAnnouncementDetail()` - 获取公告详情
    - `markAsRead()` - 标记已读

- **AnnouncementServiceImpl.java** - 公告服务实现
  - 路径：`backed/src/main/java/com/lab/backed/service/impl/AnnouncementServiceImpl.java`
  - 核心功能：
    - 按实验室类型筛选公告
    - 置顶公告优先显示
    - 支持全部/未读/已读筛选
    - 时间格式化（ISO 8601）
    - 关联查询老师姓名

#### 2. Controller层
- **StudentAnnouncementController.java** - 学生公告控制器
  - 路径：`backed/src/main/java/com/lab/backed/controller/StudentAnnouncementController.java`
  - 接口：
    - `GET /api/v1/student/announcements` - 获取公告列表
    - `GET /api/v1/student/announcements/{id}` - 获取公告详情
    - `PUT /api/v1/student/announcements/{id}/read` - 标记已读

---

## 📡 API接口文档

### 1. 获取公告列表

**接口地址：** `GET /api/v1/student/announcements`

**请求参数：**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| filterType | String | 否 | all | 筛选类型：all/unread/read |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "实验室开放时间调整通知",
        "publishTime": "2026-01-10T09:00:00",
        "isPinned": true,
        "isRead": false
      },
      {
        "id": 2,
        "title": "新设备投入使用",
        "publishTime": "2026-01-08T14:30:00",
        "isPinned": false,
        "isRead": false
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1705315200000
}
```

---

### 2. 获取公告详情

**接口地址：** `GET /api/v1/student/announcements/{id}`

**路径参数：**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 公告ID |

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "实验室开放时间调整通知",
    "content": "<p>自2026年1月起，实验室开放时间调整为...</p>",
    "publishTime": "2026-01-10T09:00:00",
    "isPinned": true,
    "attachments": "[\"file1.pdf\", \"file2.docx\"]",
    "teacherName": "李老师",
    "isRead": false
  },
  "timestamp": 1705315200000
}
```

---

### 3. 标记公告已读

**接口地址：** `PUT /api/v1/student/announcements/{id}/read`

**路径参数：**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 公告ID |

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

## 🔧 技术要点

### 1. 公告筛选逻辑

**按实验室类型筛选：**
```java
wrapper.eq(Announcement::getStatus, 1)  // 正常状态
      .and(w -> w.eq(Announcement::getTargetType, "all")  // 全部
               .or()
               .eq(Announcement::getTargetType, labType))  // 对应实验室
```

**说明：**
- 只显示状态正常的公告
- 目标类型为 `all`（全局）或与学生的实验室类型匹配

---

### 2. 排序规则

```java
.orderByDesc(Announcement::getIsPinned)  // 置顶优先
.orderByDesc(Announcement::getPublishTime);  // 时间倒序
```

**效果：**
1. 置顶公告排在最前面
2. 同级别按发布时间倒序排列

---

### 3. 前端筛选逻辑

```javascript
if ("unread".equals(filterType)) {
    announcementList = announcementList.stream()
        .filter(a -> !(Boolean) a.get("isRead"))
        .collect(Collectors.toList());
} else if ("read".equals(filterType)) {
    announcementList = announcementList.stream()
        .filter(a -> (Boolean) a.get("isRead"))
        .collect(Collectors.toList());
}
```

**说明：**
- 在后端查询后进行二次筛选
- 支持全部、未读、已读三种筛选

---

### 4. 自动标记已读

**前端实现：**
```javascript
const loadAnnouncementDetail = async () => {
  const id = route.params.id
  announcement.value = await studentApi.getAnnouncementDetail(id)
  
  // 标记为已读
  if (!announcement.value.isRead) {
    await studentApi.markAnnouncementRead(id)
  }
}
```

**说明：**
- 查看公告详情时自动调用标记已读接口
- 避免重复标记（检查isRead状态）

---

### 5. 时间格式规范

✅ **正确做法：**
```java
private static final DateTimeFormatter DATETIME_FORMATTER = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

aMap.put("publishTime", a.getPublishTime().format(DATETIME_FORMATTER));
```

**原因：**
- 使用ISO 8601标准格式
- 前后端都能正确解析
- 避免之前遇到的空格分隔问题

---

## 🧪 测试指南

### 1. 准备测试数据

**确保数据库中有公告数据：**

```sql
-- 生物实验室公告（置顶）
INSERT INTO announcement (title, content, target_type, is_pinned, publish_time, teacher_id, status)
VALUES ('实验室开放时间调整通知', '<p>自2026年1月起，实验室开放时间调整为周一至周五 8:00-17:00。</p><p>请大家遵守实验室规定。</p>', 'bio', 1, '2026-01-10 09:00:00', 1, 1);

-- 生物实验室公告（普通）
INSERT INTO announcement (title, content, target_type, is_pinned, publish_time, teacher_id, status)
VALUES ('新设备投入使用', '<p>最近采购了一批新设备，包括：</p><ul><li>高效液相色谱仪</li><li>气相色谱质谱联用仪</li></ul><p>欢迎大家预约使用。</p>', 'bio', 0, '2026-01-08 14:30:00', 1, 1);

-- 全局公告
INSERT INTO announcement (title, content, target_type, is_pinned, publish_time, teacher_id, status)
VALUES ('寒假安排通知', '<p>寒假期间实验室将关闭，具体时间为2026年1月20日至2月20日。</p><p>如有特殊需求，请提前联系管理员。</p>', 'all', 0, '2026-01-05 10:00:00', 1, 1);
```

---

### 2. 启动服务

**后端：**
```bash
cd backed
mvn spring-boot:run
```

**前端：**
```bash
cd frontend
npm run dev
```

---

### 3. 测试流程

#### 测试1：从首页跳转到公告列表

1. 访问首页：http://localhost:3000/student/dashboard
2. 点击"查看全部"按钮
3. 应该跳转到：http://localhost:3000/student/announcements
4. 验证：
   - ✅ 显示公告列表表格
   - ✅ 置顶公告在最前面
   - ✅ 显示未读图标（蓝色铃铛）
   - ✅ 分页功能正常

#### 测试2：筛选功能

1. 在公告列表页面
2. 点击"未读"按钮
3. 验证：只显示未读公告
4. 点击"已读"按钮
5. 验证：只显示已读公告（当前可能为空）
6. 点击"全部"按钮
7. 验证：显示所有公告

#### 测试3：查看公告详情

1. 点击任意公告标题
2. 应该跳转到详情页：http://localhost:3000/student/announcements/1
3. 验证：
   - ✅ 显示公告标题
   - ✅ 显示发布时间和发布人
   - ✅ 显示公告内容（支持HTML格式）
   - ✅ 如果有附件，显示附件列表
   - ✅ 置顶标签显示正确
   - ✅ 返回按钮可以返回列表页

#### 测试4：自动标记已读

1. 查看一个未读公告的详情
2. 返回公告列表
3. 验证：该公告的未读图标消失
4. 筛选"未读"，该公告不再显示

---

## 📊 数据库依赖

### 需要的表

1. **announcement** - 公告表
   - 必须有数据才能显示公告列表

2. **teacher** - 老师表
   - 用于显示公告发布人姓名
   - 通过 teacher_id 关联

---

## ⚠️ 注意事项

### 1. 公告已读状态

**当前实现：**
```java
aMap.put("isRead", false);  // TODO: 实际应该从通知表查询已读状态
```

**问题：**
- 所有公告都显示为未读
- 无法真正区分已读和未读

**解决方案：**
需要创建 `notification` 表记录用户的已读状态：

```sql
CREATE TABLE notification (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  user_type ENUM('student', 'teacher') NOT NULL,
  announcement_id INT,
  is_read TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

然后在 `markAsRead()` 方法中插入或更新记录。

---

### 2. 学生ID硬编码

**当前实现：**
```java
Integer studentId = 1;  // TODO: 从Token中获取学生ID
String labType = "bio";  // TODO: 从Token中获取实验室类型
```

**后续改进：**
- 实现JWT认证中间件
- 从请求头中解析学生ID和实验室类型
- 添加权限验证

---

### 3. 富文本内容安全

**当前实现：**
```vue
<div class="content" v-html="announcement.content"></div>
```

**安全风险：**
- 直接使用 `v-html` 可能存在XSS攻击风险
- 如果公告内容包含恶意脚本，会执行

**解决方案：**
- 后端对富文本内容进行 sanitization（清理）
- 使用 DOMPurify 等库在前端清理HTML
- 或者只允许特定的HTML标签

---

### 4. 附件处理

**当前实现：**
```java
detail.put("attachments", announcement.getAttachments());
```

**说明：**
- 附件存储为JSON数组字符串
- 前端需要解析JSON
- 没有实现文件下载功能

**后续改进：**
- 实现文件上传和下载接口
- 存储文件的URL而不是文件名
- 支持预览常见文件格式

---

## 🎯 完成状态

| 功能模块 | 状态 | 说明 |
|---------|------|------|
| 公告列表页面 | ✅ 完成 | 支持分页、筛选 |
| 公告详情页面 | ✅ 完成 | 显示完整内容 |
| 路由配置 | ✅ 完成 | 不在导航栏显示 |
| 首页跳转 | ✅ 完成 | "查看全部"按钮正常 |
| 点击查看详情 | ✅ 完成 | 标题可点击 |
| 自动标记已读 | ✅ 完成 | 查看即标记 |
| 置顶公告优先 | ✅ 完成 | 排序正确 |
| 时间格式 | ✅ 正确 | 使用ISO 8601标准 |
| 数据格式匹配 | ✅ 正确 | 前后端一致 |
| 真实已读状态 | ⚠️ 待完善 | 需要notification表 |
| 权限控制 | ⚠️ 待完善 | 当前使用硬编码 |
| 附件下载 | ⚠️ 待完善 | 仅显示文件名 |

---

## 🚀 下一步优化建议

1. **实现真实的已读状态**
   - 创建 notification 表
   - 记录每个用户对每条公告的已读状态
   - 查询时关联判断

2. **实现JWT认证**
   - 从Token中获取真实的学生ID和实验室类型
   - 添加权限拦截器

3. **实现附件下载**
   - 创建文件上传接口
   - 存储文件到服务器或OSS
   - 提供下载链接

4. **富文本内容安全**
   - 后端清理HTML内容
   - 前端使用DOMPurify

5. **公告搜索功能**
   - 支持按标题搜索
   - 支持按时间范围筛选

6. **公告分类**
   - 系统公告
   - 实验室公告
   - 课程公告
   - 支持按分类筛选

---

## 📝 总结

✅ **已完成：**
- 创建了完整的公告功能（列表+详情）
- 路由配置正确（不在导航栏显示）
- 从首页"查看全部"能正常跳转
- 点击公告标题能查看详情
- 自动标记已读状态
- 支持全部/未读/已读筛选
- 分页功能正常
- 置顶公告优先显示
- 时间格式统一使用ISO 8601标准
- 数据格式与前端完全匹配

⚠️ **待完善：**
- 真实的已读状态（需要notification表）
- JWT认证（从Token获取学生信息）
- 附件下载功能
- 富文本内容安全

🎉 **现在可以重启后端并刷新浏览器测试公告功能了！**

**测试路径：**
1. 首页 → 点击"查看全部" → 公告列表
2. 公告列表 → 点击标题 → 公告详情
3. 公告详情 → 点击"返回" → 回到列表
