# 后端启动指南

## 1. 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.6+

## 2. 数据库准备

确保已执行以下SQL脚本初始化数据库：

```bash
cd backed/src/main/resources/db
# 在MySQL中执行
source schema.sql
source data.sql
```

或者使用提供的批处理脚本：

```bash
init_database.bat
```

## 3. 修改数据库配置

编辑 `application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lab-equipment-management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root        # 修改为你的MySQL用户名
    password: root        # 修改为你的MySQL密码
```

## 4. 启动后端

### 方式一：使用Maven命令

```bash
cd backed
mvn spring-boot:run
```

### 方式二：使用IDEA

1. 打开 `BackedApplication.java`
2. 右键 -> Run 'BackedApplication'

## 5. 验证启动

访问以下地址测试API：

- 获取设备分类：http://localhost:8080/api/v1/student/categories
- 获取设备列表：http://localhost:8080/api/v1/student/devices?page=1&size=12

预期响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [...],
  "timestamp": 1705315200000
}
```

## 6. 前端对接

前端已配置代理，会自动将 `/api` 请求转发到后端。

启动前端：

```bash
cd frontend
npm run dev
```

访问：http://localhost:3000

## 7. API接口说明

### 7.1 获取设备分类

**请求：**
```
GET /api/v1/student/categories
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "显微镜",
      "parentId": 0,
      "labType": "bio",
      "sortOrder": 1,
      "status": 1
    }
  ]
}
```

### 7.2 获取设备列表（分页）

**请求：**
```
GET /api/v1/student/devices?categoryId=1&status=available&keyword=显微镜&page=1&size=12
```

**参数说明：**
- `categoryId`: 分类ID（可选）
- `status`: 设备状态（可选）- available/borrowed/repair/scrap
- `keyword`: 搜索关键词（可选）
- `page`: 页码，默认1
- `size`: 每页数量，默认12

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 15,
    "list": [
      {
        "id": 1,
        "code": "DEV-BIO-001",
        "name": "光学显微镜",
        "categoryId": 1,
        "brand": "奥林巴斯",
        "model": "CX23",
        "spec": "40x-1000x",
        "location": "A栋-201-1号柜",
        "status": "available",
        "categoryName": "显微镜",
        "thumbnail": "/images/device/default.jpg",
        "isFavorited": false
      }
    ]
  },
  "timestamp": 1705315200000
}
```

### 7.3 获取设备详情

**请求：**
```
GET /api/v1/student/devices/1
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "code": "DEV-BIO-001",
    "name": "光学显微镜",
    "brand": "奥林巴斯",
    "model": "CX23",
    "spec": "40x-1000x",
    "technicalParams": "LED光源，双目镜筒",
    "location": "A栋-201-1号柜",
    "purchaseDate": "2024-03-15",
    "status": "available",
    "categoryName": "显微镜",
    "thumbnail": "/images/device/default.jpg",
    "isFavorited": false
  }
}
```

## 8. 常见问题

### Q1: 启动时报错 "Cannot resolve symbol"

**解决：** 重新加载Maven依赖
```bash
mvn clean install
```

### Q2: 数据库连接失败

**检查：**
1. MySQL服务是否启动
2. 数据库 `lab-equipment-management` 是否已创建
3. 用户名和密码是否正确

### Q3: 前端请求跨域错误

**解决：** Vite已配置代理，确保：
1. 后端运行在 8080 端口
2. 前端运行在 3000 端口
3. 前端请求路径以 `/api` 开头

## 9. 项目结构

```
backed/
├── src/main/java/com/lab/backed/
│   ├── BackedApplication.java          # 启动类
│   ├── common/                         # 通用类
│   │   ├── Result.java                 # 统一响应
│   │   └── PageResult.java             # 分页响应
│   ├── config/                         # 配置类
│   │   └── MybatisPlusConfig.java      # MyBatis-Plus配置
│   ├── controller/                     # 控制器
│   │   └── StudentDeviceController.java # 学生端设备控制器
│   ├── entity/                         # 实体类
│   │   ├── Device.java                 # 设备实体
│   │   └── DeviceCategory.java         # 设备分类实体
│   ├── mapper/                         # Mapper接口
│   │   ├── DeviceMapper.java
│   │   └── DeviceCategoryMapper.java
│   └── service/                        # 服务层
│       ├── DeviceService.java          # 服务接口
│       └── impl/
│           └── DeviceServiceImpl.java  # 服务实现
└── src/main/resources/
    ├── application.yml                 # 配置文件
    └── db/                             # 数据库脚本
        ├── schema.sql                  # 建表脚本
        └── data.sql                    # 模拟数据
```

## 10. 下一步开发

当前已完成：
- ✅ 设备分类查询
- ✅ 设备列表查询（支持筛选、搜索、分页）
- ✅ 设备详情查询

待实现：
- ⏳ 设备收藏功能
- ⏳ 设备图片管理
- ⏳ JWT认证与授权
- ⏳ 预约功能
- ⏳ 借用管理功能
