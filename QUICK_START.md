# 实验室设备管理系统 - 快速启动指南

## 🚀 5分钟快速启动

### 前置要求

- ✅ JDK 17+
- ✅ MySQL 8.0+
- ✅ Node.js 16+
- ✅ Maven 3.6+

### 步骤1：初始化数据库

```bash
# 进入数据库脚本目录
cd backed/src/main/resources/db

# 在MySQL中执行（或使用提供的批处理脚本）
mysql -u root -p lab-equipment-management < schema.sql
mysql -u root -p lab-equipment-management < data.sql

# 或者使用Windows批处理脚本（推荐）
init_database.bat
```

### 步骤2：配置数据库连接

编辑 `backed/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    username: root        # 修改为你的MySQL用户名
    password: your_password  # 修改为你的MySQL密码
```

### 步骤3：启动后端

```bash
cd backed
mvn spring-boot:run
```

看到以下日志表示启动成功：
```
Started BackedApplication in X.XXX seconds
```

**验证后端：**
浏览器访问 http://localhost:8080/api/v1/student/categories

### 步骤4：启动前端

```bash
cd frontend
npm install  # 首次运行需要安装依赖
npm run dev
```

**访问系统：**
浏览器打开 http://localhost:3000

### 步骤5：登录测试

使用模拟账号登录：
- **学号：** `2024001`
- **密码：** 任意输入（当前未实现认证）

登录后自动跳转到设备查询页面。

## 📱 功能演示

### 设备查询页面

访问：http://localhost:3000/student/devices

**功能列表：**
- ✅ 查看设备列表（卡片式布局）
- ✅ 按分类筛选（显微镜、离心机等8个分类）
- ✅ 按状态筛选（可借用、维修中等）
- ✅ 关键词搜索（支持名称、编号、品牌、型号）
- ✅ 分页浏览（12/24/36/48条可选）
- ✅ 收藏设备（点击心形图标）
- ✅ 响应式设计（适配手机、平板、电脑）

## 🔧 常见问题

### Q1: 后端启动失败 "Cannot resolve symbol"

**解决：**
```bash
cd backed
mvn clean install
mvn spring-boot:run
```

### Q2: 数据库连接失败

**检查清单：**
1. MySQL服务是否启动？
2. 数据库 `lab-equipment-management` 是否已创建？
3. `application.yml` 中的用户名和密码是否正确？

**测试连接：**
```bash
mysql -u root -p -e "USE lab-equipment-management; SELECT COUNT(*) FROM device;"
```

### Q3: 前端请求失败 "Network Error"

**解决：**
1. 确认后端正在运行（访问 http://localhost:8080/api/v1/student/categories）
2. 检查 `frontend/vite.config.js` 代理配置
3. 重启前端服务

### Q4: 页面无数据

**可能原因：**
- 数据库中没有数据
- 筛选条件过于严格

**解决：**
```sql
-- 检查数据是否存在
SELECT COUNT(*) FROM device;
SELECT COUNT(*) FROM device_category;

-- 清除筛选条件重新搜索
```

## 📂 项目结构

```
lab-equipment-management/
├── backed/                          # 后端项目
│   ├── src/main/java/com/lab/backed/
│   │   ├── controller/              # 控制器层
│   │   ├── service/                 # 服务层
│   │   ├── mapper/                  # 数据访问层
│   │   ├── entity/                  # 实体类
│   │   ├── common/                  # 通用类
│   │   └── config/                  # 配置类
│   ├── src/main/resources/
│   │   ├── application.yml          # 配置文件
│   │   └── db/                      # 数据库脚本
│   ├── pom.xml                      # Maven配置
│   └── BACKEND_STARTUP.md           # 后端启动指南
│
├── frontend/                        # 前端项目
│   ├── src/
│   │   ├── views/student/           # 学生端页面
│   │   ├── api/student/             # 学生端API
│   │   └── utils/                   # 工具类
│   ├── vite.config.js               # Vite配置
│   └── package.json
│
├── requirement.md                   # 需求文档
├── INTEGRATION_GUIDE.md             # 前后端联调指南
└── BACKEND_COMPLETION_REPORT.md     # 后端完成报告
```

## 📖 详细文档

| 文档 | 说明 |
|------|------|
| [BACKEND_STARTUP.md](backed/BACKEND_STARTUP.md) | 后端详细启动指南 |
| [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) | 前后端联调完整指南 |
| [BACKEND_COMPLETION_REPORT.md](BACKEND_COMPLETION_REPORT.md) | 后端实现完成报告 |
| [backed/src/main/resources/db/README.md](backed/src/main/resources/db/README.md) | 数据库初始化说明 |
| [backed/src/main/resources/db/DATA_DICTIONARY.md](backed/src/main/resources/db/DATA_DICTIONARY.md) | 数据字典 |

## 🎯 API接口测试

### 方法1：浏览器直接访问

```
# 获取设备分类
http://localhost:8080/api/v1/student/categories

# 获取设备列表
http://localhost:8080/api/v1/student/devices?page=1&size=12

# 获取设备详情
http://localhost:8080/api/v1/student/devices/1
```

### 方法2：使用测试脚本

```bash
cd backed
test_api.bat
```

### 方法3：使用Postman/Apifox

导入接口文档，测试所有API。

## 🔗 相关链接

- **前端首页：** http://localhost:3000
- **后端API：** http://localhost:8080/api/v1
- **设备查询：** http://localhost:3000/student/devices
- **GitHub仓库：** （待添加）

## 💡 开发提示

### 热重载

- **前端：** Vite支持热重载，修改代码后自动刷新
- **后端：** 需要重启服务，或添加 `spring-boot-devtools` 实现热部署

### 调试技巧

**前端调试：**
- 打开浏览器开发者工具（F12）
- 查看 Console 标签的错误信息
- 查看 Network 标签的请求和响应

**后端调试：**
- 查看控制台输出的SQL语句
- 在IDEA中设置断点调试
- 查看 `target/spring-boot.log` 日志文件

## 📊 当前进度

### 已完成 ✅

- ✅ 数据库设计与初始化（13张表，89条模拟数据）
- ✅ 后端基础架构（Spring Boot + MyBatis-Plus）
- ✅ 设备分类查询API
- ✅ 设备列表查询API（支持筛选、搜索、分页）
- ✅ 设备详情查询API
- ✅ 前端设备查询页面
- ✅ 前后端对接完成

### 进行中 🚧

- ⏳ JWT认证与授权
- ⏳ 设备收藏功能
- ⏳ 设备图片管理

### 计划中 📋

- 📝 预约功能
- 📝 借用管理
- 📝 老师端功能
- 📝 数据统计与报表

## 🆘 获取帮助

如遇到问题：

1. 查看详细文档（见上方"详细文档"部分）
2. 检查常见问题（见上方"常见问题"部分）
3. 查看后端日志和控制台输出
4. 检查浏览器开发者工具的Network标签

## 🎉 开始使用

现在你已经准备好了！访问 http://localhost:3000 开始使用实验室设备管理系统吧！

---

**最后更新：** 2026-05-19  
**版本：** v1.0
