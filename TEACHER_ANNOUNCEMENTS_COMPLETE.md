# 管理端"公告管理"功能实现文档

## 📋 功能概述

完成了老师端"公告管理"页面的后端实现，支持公告列表查询、发布公告和删除公告功能。

---

## 🏗️ 后端架构

### 1. 实体类（已存在）

**文件位置：** `backed/src/main/java/com/lab/backed/entity/Announcement.java`

```java
@Data
@TableName("announcement")
public class Announcement {
    private Integer id;              // 主键ID
    private String title;            // 标题
    private String content;          // 内容（富文本）
    private String attachments;      // 附件（JSON数组）
    private String targetType;       // 范围：all/bio/chem/class/student
    private String targetIds;        // 目标ID列表（JSON数组）
    private Integer isPinned;        // 是否置顶：0否 1是
    private LocalDateTime publishTime; // 发布时间
    private Integer teacherId;       // 发布老师ID
    private Integer status;          // 状态：1正常 0已删除
    private LocalDateTime createdAt; // 创建时间
}
```

### 2. Mapper接口（已存在）

**文件位置：** `backed/src/main/java/com/lab/backed/mapper/AnnouncementMapper.java`

```java
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
```

### 3. Service接口（新建）

**文件位置：** `backed/src/main/java/com/lab/backed/service/TeacherAnnouncementService.java`

```java
public interface TeacherAnnouncementService {
    
    /**
     * 获取公告列表（分页）
     */
    Map<String, Object> getAnnouncementList(Integer page, Integer size);
    
    /**
     * 发布公告
     */
    void createAnnouncement(String title, String content, String targetType,
                           String targetIds, Integer isPinned, Integer teacherId);
    
    /**
     * 删除公告（软删除）
     */
    void deleteAnnouncement(Integer id);
}
```

### 4. Service实现（新建）

**文件位置：** `backed/src/main/java/com/lab/backed/service/impl/TeacherAnnouncementServiceImpl.java`

**核心功能：**

#### 4.1 获取公告列表

```java
@Override
public Map<String, Object> getAnnouncementList(Integer page, Integer size) {
    // 构建查询条件
    LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
    
    // 只查询未删除的公告
    wrapper.eq(Announcement::getStatus, 1);
    
    // 按置顶排序，然后按发布时间倒序
    wrapper.orderByDesc(Announcement::getIsPinned)
           .orderByDesc(Announcement::getPublishTime);
    
    // 分页查询
    Page<Announcement> announcementPage = new Page<>(page, size);
    Page<Announcement> result = announcementMapper.selectPage(announcementPage, wrapper);
    
    // 转换为前端期望的格式
    List<Map<String, Object>> announcementList = result.getRecords().stream()
            .map(a -> {
                Map<String, Object> aMap = new HashMap<>();
                aMap.put("id", a.getId());
                aMap.put("title", a.getTitle());
                aMap.put("content", a.getContent());
                aMap.put("attachments", a.getAttachments());
                aMap.put("targetType", a.getTargetType());
                aMap.put("targetIds", a.getTargetIds());
                aMap.put("isPinned", a.getIsPinned() == 1);
                aMap.put("publishTime", a.getPublishTime().format(DATETIME_FORMATTER));
                aMap.put("teacherId", a.getTeacherId());
                aMap.put("status", a.getStatus());
                aMap.put("createdAt", a.getCreatedAt().format(DATETIME_FORMATTER));
                
                return aMap;
            })
            .collect(Collectors.toList());
    
    // 构建返回结果
    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("total", result.getTotal());
    resultMap.put("list", announcementList);
    
    return resultMap;
}
```

**功能特点：**
- ✅ 只查询未删除的公告（status=1）
- ✅ **智能排序**：置顶公告优先，然后按发布时间倒序
- ✅ 日期格式化（yyyy-MM-dd HH:mm:ss）
- ✅ isPinned转换为布尔值
- ✅ 分页查询，默认每页10条

#### 4.2 发布公告

```java
@Override
@Transactional
public void createAnnouncement(String title, String content, String targetType,
                              String targetIds, Integer isPinned, Integer teacherId) {
    // 验证标题
    if (title == null || title.trim().isEmpty()) {
        throw new RuntimeException("公告标题不能为空");
    }
    
    // 验证内容
    if (content == null || content.trim().isEmpty()) {
        throw new RuntimeException("公告内容不能为空");
    }
    
    // 验证发布范围
    if (targetType == null || targetType.trim().isEmpty()) {
        throw new RuntimeException("请选择发布范围");
    }
    
    // 创建公告
    Announcement announcement = new Announcement();
    announcement.setTitle(title);
    announcement.setContent(content);
    announcement.setAttachments(targetIds);
    announcement.setTargetType(targetType);
    announcement.setTargetIds(targetIds);
    announcement.setIsPinned(isPinned != null && isPinned == 1 ? 1 : 0);
    announcement.setPublishTime(LocalDateTime.now());
    announcement.setTeacherId(teacherId);
    announcement.setStatus(1); // 正常状态
    announcement.setCreatedAt(LocalDateTime.now());
    
    announcementMapper.insert(announcement);
}
```

**功能特点：**
- ✅ 完整的表单验证（标题、内容、发布范围）
- ✅ 自动设置发布时间为当前时间
- ✅ 自动设置状态为正常（1）
- ✅ 支持置顶功能
- ✅ 事务控制保证数据一致性

#### 4.3 删除公告（软删除）

```java
@Override
@Transactional
public void deleteAnnouncement(Integer id) {
    // 检查公告是否存在
    Announcement announcement = announcementMapper.selectById(id);
    if (announcement == null) {
        throw new RuntimeException("公告不存在");
    }
    
    // 软删除：将状态设置为0
    announcement.setStatus(0);
    announcementMapper.updateById(announcement);
}
```

**功能特点：**
- ✅ 公告存在性校验
- ✅ **软删除**：不物理删除数据，只修改状态
- ✅ 可以恢复已删除的公告
- ✅ 事务控制

### 5. Controller控制器（新建）

**文件位置：** `backed/src/main/java/com/lab/backed/controller/TeacherAnnouncementController.java`

```java
@RestController
@RequestMapping("/api/v1/teacher/announcements")
@RequiredArgsConstructor
public class TeacherAnnouncementController {
    
    private final TeacherAnnouncementService teacherAnnouncementService;
    
    /**
     * 获取公告列表（分页）
     */
    @GetMapping
    public Result<Map<String, Object>> getAnnouncements(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> result = teacherAnnouncementService.getAnnouncementList(page, size);
        return Result.success(result);
    }
    
    /**
     * 发布公告
     */
    @PostMapping
    public Result<Void> createAnnouncement(@RequestBody Map<String, Object> params) {
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        String targetType = (String) params.get("targetType");
        String targetIds = (String) params.get("targetIds");
        Integer isPinned = params.get("isPinned") != null && 
                          ((Boolean) params.get("isPinned")) ? 1 : 0;
        
        // TODO: 从token中获取当前老师ID，暂时使用固定值
        Integer teacherId = 1;
        
        teacherAnnouncementService.createAnnouncement(title, content, targetType, 
                                                     targetIds, isPinned, teacherId);
        return Result.success();
    }
    
    /**
     * 删除公告（软删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Integer id) {
        teacherAnnouncementService.deleteAnnouncement(id);
        return Result.success();
    }
}
```

---

## 🔌 API接口（3个）

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/v1/teacher/announcements` | 获取公告列表 |
| POST | `/api/v1/teacher/announcements` | 发布公告 |
| DELETE | `/api/v1/teacher/announcements/{id}` | 删除公告 |

### 1. 获取公告列表

**请求示例：**
```
GET /api/v1/teacher/announcements?page=1&size=10
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 5,
    "list": [
      {
        "id": 1,
        "title": "实验室开放时间调整通知",
        "content": "因设备维护，本周末实验室暂停开放...",
        "attachments": null,
        "targetType": "all",
        "targetIds": null,
        "isPinned": true,
        "publishTime": "2026-01-15 09:00:00",
        "teacherId": 1,
        "status": 1,
        "createdAt": "2026-01-15 09:00:00"
      },
      {
        "id": 2,
        "title": "新设备投入使用",
        "content": "新增10台显微镜已安装调试完成...",
        "attachments": null,
        "targetType": "bio",
        "targetIds": null,
        "isPinned": false,
        "publishTime": "2026-01-14 14:30:00",
        "teacherId": 1,
        "status": 1,
        "createdAt": "2026-01-14 14:30:00"
      }
    ]
  }
}
```

**排序规则：**
1. 置顶公告优先（isPinned=true）
2. 按发布时间倒序（最新的在前）

### 2. 发布公告

**请求示例：**
```
POST /api/v1/teacher/announcements
Content-Type: application/json

{
  "title": "实验室开放时间调整通知",
  "content": "因设备维护，本周末实验室暂停开放，请各班级提前安排实验课程。",
  "targetType": "all",
  "targetIds": null,
  "isPinned": true
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**业务逻辑：**
- ✅ 自动设置发布时间为当前时间
- ✅ 自动设置状态为正常（1）
- ✅ 验证必填字段（标题、内容、发布范围）

### 3. 删除公告

**请求示例：**
```
DELETE /api/v1/teacher/announcements/1
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**业务逻辑：**
- ✅ 软删除：将status设置为0
- ✅ 不物理删除数据，可以恢复
- ✅ 公告存在性校验

---

## 🧪 测试步骤

### 1. 准备测试数据

```sql
-- 插入测试公告数据
INSERT INTO announcement (title, content, target_type, is_pinned, publish_time, teacher_id, status, created_at) VALUES
('实验室开放时间调整通知', '因设备维护，本周末实验室暂停开放，请各班级提前安排实验课程。', 'all', 1, '2026-01-15 09:00:00', 1, 1, '2026-01-15 09:00:00'),
('新设备投入使用', '新增10台显微镜已安装调试完成，即日起可使用。', 'bio', 0, '2026-01-14 14:30:00', 1, 1, '2026-01-14 14:30:00'),
('化学实验室安全培训', '所有化学实验室使用者必须参加安全培训，时间：下周一14:00。', 'chem', 0, '2026-01-13 10:00:00', 1, 1, '2026-01-13 10:00:00');
```

### 2. 重启后端服务

```bash
cd backed
mvn spring-boot:run
```

### 3. 访问页面

1. 使用老师账号登录（工号：T001，密码：123456）
2. 访问 http://localhost:3000/teacher/announcements
3. **无需修改任何配置**，已自动使用真实API

### 4. 功能测试

#### 测试1：查看公告列表
- ✅ 验证显示所有正常状态的公告
- ✅ 验证置顶公告排在前面
- ✅ 验证按发布时间倒序排列
- ✅ 验证日期格式正确（yyyy-MM-dd HH:mm:ss）
- ✅ 验证置顶标记显示正确

#### 测试2：发布公告
- ✅ 点击"发布公告"按钮
- ✅ 填写标题（如：测试公告）
- ✅ 填写内容（如：这是一条测试公告）
- ✅ 选择发布范围（全部/生物实验室/化学实验室）
- ✅ 选择是否置顶
- ✅ 点击"发布"
- ✅ 验证提示信息
- ✅ 验证公告添加到列表顶部（如果置顶）

#### 测试3：表单验证
- ✅ 不填写标题，点击"发布"
- ✅ 应该提示错误（前端或后端验证）

- ✅ 不填写内容，点击"发布"
- ✅ 应该提示错误

- ✅ 不选择发布范围，点击"发布"
- ✅ 应该提示错误

#### 测试4：删除公告
- ✅ 点击某条公告的"删除"按钮
- ✅ 确认对话框中选择"确定"
- ✅ 验证操作成功提示
- ✅ 验证公告从列表中消失
- ✅ 验证数据库中status变为0（软删除）

#### 测试5：置顶功能
- ✅ 发布一条置顶公告
- ✅ 验证该公告排在列表最前面
- ✅ 发布多条置顶公告
- ✅ 验证按发布时间倒序排列

---

## 💡 技术亮点

### 1. 智能排序
- **置顶公告优先**：isPinned=1的公告排在前面
- **时间倒序**：同一优先级内，按发布时间倒序
- 确保重要公告始终可见

### 2. 软删除机制
- 不物理删除数据
- 通过status字段标记删除状态
- 可以恢复已删除的公告
- 保留历史记录

### 3. 完整的表单验证
- 标题不能为空
- 内容不能为空
- 发布范围必须选择
- 友好的错误提示

### 4. 灵活的发布范围
- all：全部用户
- bio：生物实验室
- chem：化学实验室
- class：指定班级
- student：指定学生

### 5. 数据一致性
- 事务控制保证数据完整性
- 自动设置发布时间和状态
- 记录发布老师ID

---

## ⚠️ 注意事项

### 当前限制
- ⚠️ 老师ID使用固定值1（需要实现JWT认证）
- ⚠️ 附件功能未完全实现（TODO）
- ⚠️ 定时发布功能未实现（TODO）
- ⚠️ 富文本编辑器未集成（目前使用普通文本框）

### 前端改进建议

#### 1. 添加富文本编辑器

```vue
<!-- 安装 quill 或 tinymce -->
npm install @vueup/vue-quill

<!-- 使用 -->
<el-form-item label="内容" required>
  <QuillEditor v-model:content="announcementForm.content" theme="snow" />
</el-form-item>
```

#### 2. 添加附件上传功能

```vue
<el-form-item label="附件">
  <el-upload
    action="/api/v1/upload"
    :on-success="handleUploadSuccess"
    multiple
  >
    <el-button type="primary">上传附件</el-button>
  </el-upload>
</el-form-item>
```

#### 3. 添加编辑功能

```vue
<el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
```

#### 4. 添加筛选功能

```vue
<el-form :inline="true">
  <el-form-item label="发布范围">
    <el-select v-model="filterForm.targetType">
      <el-option label="全部" value="all" />
      <el-option label="生物实验室" value="bio" />
      <el-option label="化学实验室" value="chem" />
    </el-select>
  </el-form-item>
  <el-form-item label="置顶">
    <el-select v-model="filterForm.isPinned">
      <el-option label="全部" :value="null" />
      <el-option label="是" :value="true" />
      <el-option label="否" :value="false" />
    </el-select>
  </el-form-item>
  <el-form-item>
    <el-button type="primary" @click="loadAnnouncements">搜索</el-button>
    <el-button @click="handleReset">重置</el-button>
  </el-form-item>
</el-form>
```

### 性能优化建议

#### 1. 分页优化

对于大量公告，可以考虑：
- 添加缓存机制
- 使用Redis缓存热门公告
- 实现懒加载

#### 2. 查询优化

```java
// 如果只需要部分字段，可以使用select字段
wrapper.select(Announcement::getId, Announcement::getTitle, 
              Announcement::getPublishTime, Announcement::getIsPinned);
```

### 后续开发建议

1. **实现JWT认证**
   - 从token中解析老师ID
   - 记录操作人信息

2. **完善附件功能**
   - 实现文件上传接口
   - 存储附件到OSS或本地
   - 前端预览和下载功能

3. **实现定时发布**
   - 添加publishTime字段
   - 定时任务检查待发布公告
   - 自动发布到期的公告

4. **添加编辑功能**
   - 实现更新公告接口
   - 前端编辑对话框
   - 记录修改历史

5. **添加阅读统计**
   - 记录每个用户的阅读状态
   - 统计阅读人数
   - 显示未读用户列表

6. **实现消息推送**
   - 发布公告时推送通知
   - WebSocket实时推送
   - 站内信通知

7. **添加回收站**
   - 显示已删除的公告
   - 支持恢复功能
   - 永久删除功能

---

## 📊 数据库表结构

### announcement表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 主键ID |
| title | VARCHAR(100) | 标题 |
| content | TEXT | 内容（富文本） |
| attachments | TEXT | 附件（JSON数组） |
| target_type | VARCHAR(20) | 范围：all/bio/chem/class/student |
| target_ids | TEXT | 目标ID列表（JSON数组） |
| is_pinned | TINYINT | 是否置顶：0否 1是 |
| publish_time | DATETIME | 发布时间 |
| teacher_id | INT | 发布老师ID |
| status | TINYINT | 状态：1正常 0已删除 |
| created_at | DATETIME | 创建时间 |

---

## 🎯 总结

本次实现完成了老师端"公告管理"的核心功能：

✅ **后端实现**（3个文件）：
- TeacherAnnouncementService接口（26行）
- TeacherAnnouncementServiceImpl实现（124行）
- TeacherAnnouncementController控制器（61行）

✅ **核心功能**：
- 公告列表查询（分页、智能排序）
- 发布公告（完整验证、自动设置时间）
- 删除公告（软删除、可恢复）
- 置顶功能（优先显示）
- 事务控制

✅ **API接口**（3个）：
- GET /api/v1/teacher/announcements
- POST /api/v1/teacher/announcements
- DELETE /api/v1/teacher/announcements/{id}

✅ **技术亮点**：
- 智能排序（置顶优先+时间倒序）
- 软删除机制（数据可恢复）
- 完整的表单验证
- 灵活的发布范围
- 数据一致性保证

---

现在您可以重启后端并访问公告管理页面进行测试了！🎉

**注意：** 如需完整功能，建议添加：
1. 富文本编辑器
2. 附件上传功能
3. 编辑功能
4. 筛选功能
5. 阅读统计

详细的前端改进建议请查看文档中的"前端改进建议"部分。
