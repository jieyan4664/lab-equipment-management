# 前后端联调指南 - 设备查询功能

## 📋 功能概述

已完成学生端"设备查询"功能的前后端对接，包括：
- ✅ 设备分类列表查询
- ✅ 设备列表分页查询（支持筛选、搜索）
- ✅ 设备详情查询

## 🚀 快速启动

### 1. 启动后端服务

```bash
cd backed
mvn spring-boot:run
```

等待看到以下日志表示启动成功：
```
Started BackedApplication in X.XXX seconds
```

**验证后端：**
访问 http://localhost:8080/api/v1/student/categories

### 2. 启动前端服务

```bash
cd frontend
npm run dev
```

访问 http://localhost:3000

### 3. 登录测试

使用模拟账号登录：
- 学号：`2024001`
- 密码：任意（当前未实现认证）

### 4. 访问设备查询页面

登录后自动跳转到设备查询页面，或直接访问：
http://localhost:3000/student/devices

## 🔧 配置说明

### 后端配置

**文件：** `backed/src/main/resources/application.yml`

```yaml
server:
  port: 8080  # 后端端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lab-equipment-management
    username: root      # 修改为你的MySQL用户名
    password: root      # 修改为你的MySQL密码
```

### 前端配置

**文件：** `frontend/vite.config.js`

```javascript
server: {
  port: 3000,  // 前端端口
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 代理到后端
      changeOrigin: true
    }
  }
}
```

**文件：** `frontend/src/api/student/index.js`

```javascript
const USE_MOCK = false  // 已关闭Mock，使用真实API
```

## 📡 API接口映射

### 前端调用 → 后端接口

| 前端方法 | 请求方式 | 后端URL | 说明 |
|---------|---------|---------|------|
| `getCategories()` | GET | `/api/v1/student/categories` | 获取设备分类 |
| `getDevices(params)` | GET | `/api/v1/student/devices` | 获取设备列表 |
| `getDeviceDetail(id)` | GET | `/api/v1/student/devices/{id}` | 获取设备详情 |

### 请求参数示例

**获取设备列表：**
```javascript
// 前端调用
studentApi.getDevices({
  categoryId: 1,        // 可选：分类ID
  status: 'available',  // 可选：设备状态
  keyword: '显微镜',     // 可选：搜索关键词
  page: 1,              // 页码
  size: 12              // 每页数量
})

// 实际请求
GET /api/v1/student/devices?categoryId=1&status=available&keyword=显微镜&page=1&size=12
```

## 🧪 测试场景

### 场景1：查看所有设备

1. 进入设备查询页面
2. 不选择任何筛选条件
3. 点击"搜索"按钮
4. **预期结果：** 显示所有15个设备，分页显示

### 场景2：按分类筛选

1. 在"设备分类"下拉框选择"显微镜"
2. 点击"搜索"按钮
3. **预期结果：** 只显示显微镜类设备（2个）

### 场景3：按状态筛选

1. 在"设备状态"下拉框选择"可借用"
2. 点击"搜索"按钮
3. **预期结果：** 只显示状态为available的设备

### 场景4：关键词搜索

1. 在搜索框输入"离心机"
2. 等待500ms（防抖）
3. **预期结果：** 自动搜索，显示离心机相关设备（2个）

### 场景5：组合筛选

1. 选择分类："生物设备"
2. 选择状态："可借用"
3. 输入关键词："显微镜"
4. 点击"搜索"
5. **预期结果：** 显示同时满足三个条件的设备

### 场景6：分页功能

1. 设置每页显示24条
2. 点击第2页
3. **预期结果：** 显示第2页的设备数据

### 场景7：收藏功能

1. 点击某个设备的"收藏"图标
2. **预期结果：** 
   - 图标变为实心红色
   - 提示"已添加到收藏"
3. 再次点击
4. **预期结果：**
   - 图标变为空心
   - 提示"已取消收藏"

## 🔍 调试技巧

### 1. 查看网络请求

打开浏览器开发者工具（F12）→ Network标签：

**检查点：**
- 请求URL是否正确：`/api/v1/student/devices`
- 请求参数是否正确传递
- 响应状态码是否为200
- 响应数据结构是否符合预期

### 2. 查看后端日志

后端控制台会输出SQL语句：

```sql
SELECT * FROM device 
WHERE category_id = 1 
  AND status = 'available' 
  AND (name LIKE '%显微镜%' OR code LIKE '%显微镜%')
ORDER BY created_at DESC 
LIMIT 12 OFFSET 0
```

### 3. 常见问题排查

#### 问题1：前端请求失败，报错 "Network Error"

**可能原因：**
- 后端未启动
- 端口配置错误
- 代理配置错误

**解决方法：**
1. 确认后端运行在8080端口
2. 检查 `vite.config.js` 代理配置
3. 重启前端服务

#### 问题2：返回空数据

**可能原因：**
- 数据库中没有数据
- 筛选条件过于严格

**解决方法：**
1. 检查数据库是否有数据：`SELECT COUNT(*) FROM device;`
2. 清除所有筛选条件重新搜索
3. 查看后端日志中的SQL语句

#### 问题3：跨域错误

**可能原因：**
- 代理配置未生效

**解决方法：**
1. 确认请求URL以 `/api` 开头
2. 重启前端服务使配置生效
3. 检查浏览器控制台是否有CORS错误

## 📊 数据流程

```
用户操作
  ↓
Vue组件 (Devices.vue)
  ↓
API调用 (studentApi.getDevices)
  ↓
Axios请求 (/api/v1/student/devices)
  ↓
Vite代理 (转发到 http://localhost:8080)
  ↓
Spring Boot Controller (StudentDeviceController)
  ↓
Service层 (DeviceServiceImpl)
  ↓
MyBatis-Plus Mapper (DeviceMapper)
  ↓
MySQL数据库
  ↓
返回数据 ← ← ← ← ← ← ← ← ← ← ← ← ← ← ← ←
  ↓
前端渲染设备卡片
```

## 🎯 性能优化建议

### 1. 数据库索引

确保以下字段有索引（已在schema.sql中创建）：

```sql
-- 设备表索引
ALTER TABLE device ADD INDEX idx_category_id (category_id);
ALTER TABLE device ADD INDEX idx_status (status);
ALTER TABLE device ADD INDEX idx_name (name);
ALTER TABLE device ADD INDEX idx_code (code);
```

### 2. 前端防抖

搜索框已实现500ms防抖，避免频繁请求。

### 3. 图片懒加载

后续可以实现图片懒加载，提升首屏加载速度。

## 📝 下一步开发计划

### 短期（本周）
- [ ] 实现设备收藏功能（需要创建收藏表）
- [ ] 实现设备图片上传和展示
- [ ] 添加JWT认证

### 中期（本月）
- [ ] 实现预约功能
- [ ] 实现借用管理
- [ ] 实现老师端设备管理

### 长期
- [ ] WebSocket实时通知
- [ ] 数据统计与报表
- [ ] 二维码扫描功能

## 🆘 技术支持

如遇到问题，请检查：

1. **Java版本：** 确保使用JDK 17+
2. **Maven版本：** 确保使用Maven 3.6+
3. **MySQL版本：** 确保使用MySQL 8.0+
4. **Node版本：** 确保使用Node 16+

查看详细日志：
- 后端日志：`backed/target/spring-boot.log`
- 前端日志：浏览器控制台（F12）

---

**祝开发顺利！** 🎉
