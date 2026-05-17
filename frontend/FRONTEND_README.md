# 实验室设备管理系统 - 前端

基于 Vue3 + Vite + Element Plus 开发的实验室设备管理系统前端应用。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **图表**: ECharts
- **日期处理**: Day.js

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API接口
│   │   ├── common/       # 公共接口
│   │   ├── student/      # 学生端接口
│   │   └── teacher/      # 老师端接口
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── composables/      # 组合式函数
│   ├── layouts/          # 布局组件
│   ├── router/           # 路由配置
│   ├── stores/           # 状态管理
│   │   └── modules/      # 模块store
│   ├── utils/            # 工具函数
│   ├── views/            # 页面组件
│   │   ├── common/       # 公共页面
│   │   ├── student/      # 学生端页面
│   │   └── teacher/      # 老师端页面
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
└── package.json
```

## 功能模块

### 公共功能
- ✅ 用户登录（学生/老师）
- ✅ 消息通知
- ✅ 全局搜索

### 学生端
- ✅ 首页仪表盘
- ✅ 设备查询与浏览
- ✅ 设备详情与预约
- ✅ 我的预约管理
- ✅ 我的借用记录
- ✅ 个人中心

### 老师端
- ✅ 管理仪表盘
- ✅ 设备管理（增删改查、二维码生成）
- ✅ 预约审核
- ✅ 借用/归还管理
- ✅ 学生管理
- ✅ 维修/报废管理
- ✅ 公告管理
- ✅ 数据统计与报表
- ✅ 系统设置

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5174

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 测试账号

**学生账号:**
- 学号: 20240001
- 密码: 123456

**老师账号:**
- 工号: T001
- 密码: 123456

## Mock数据

项目当前使用Mock数据进行开发，所有API调用都返回模拟数据。要切换到真实后端API，只需将各API文件中的 `USE_MOCK` 常量设置为 `false`。

```javascript
const USE_MOCK = true  // 改为 false 使用真实API
```

## 主要特性

1. **响应式设计**: 适配不同屏幕尺寸
2. **路由守卫**: 基于角色的访问控制
3. **状态管理**: 使用Pinia进行全局状态管理
4. **Mock数据**: 完整的Mock数据支持独立前端开发
5. **图表展示**: 使用ECharts展示统计数据
6. **组件化**: 高度组件化的代码结构
7. **Type Safety**: 良好的类型提示（可后续升级为TypeScript）

## 注意事项

1. 所有页面都已实现基本功能和交互
2. 使用了Element Plus组件库，界面美观统一
3. 集成了ECharts用于数据可视化
4. 实现了完整的路由权限控制
5. Mock数据覆盖了所有主要功能场景

## 后续优化建议

1. 添加TypeScript支持
2. 完善表单验证规则
3. 添加更多动画效果
4. 优化移动端适配
5. 添加单元测试
6. 性能优化（懒加载、代码分割等）
7. 接入真实后端API
