# 大学实验室设备管理系统

> 面向高校生物 / 化学实验室的综合设备管理平台（毕业设计）

## 📋 项目简介

本系统基于 **Spring Boot 3 + Vue 3** 开发，提供实验室设备全生命周期管理，涵盖：

- 🔬 **设备档案管理** — 设备台账、二维码、批量导入
- 📅 **借用预约** — 日历防冲突、双审批、超期提醒
- 🔧 **维护保养** — 故障报修、自动周期提醒、费用统计
- 🧪 **耗材 / 危化品管控** — CAS 号登记、GHS 标签、领用限额
- 🛡️ **安全管理** — 持证校验、事故上报、操作日志
- 📊 **统计看板** — ECharts 多维可视化分析
- 👥 **用户权限** — RBAC 五角色体系

## 🛠️ 技术栈

| 层次 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3 + MyBatis-Plus |
| 前端框架 | Vue 3 + Element Plus + ECharts 5 |
| 数据库 | MySQL 8 + Redis |
| 安全 | Spring Security + JWT |
| 构建工具 | Maven + Vite |

## 🚀 快速开始

### 后端

```bash
# 1. 修改 src/main/resources/application.yml 中的数据库配置
# 2. 执行 SQL 初始化脚本
# 3. 启动
mvn spring-boot:run
```

### 前端

```bash
cd frontend
npm install
npm run dev
```

### 预览（静态原型）

直接用浏览器打开 `lab-management.html` 即可查看完整 UI 原型。

## 📁 目录结构

```
├── lab-management.html     # 前端 UI 原型（单文件）
├── src/                    # Spring Boot 后端源码（待开发）
├── frontend/               # Vue 3 前端工程（待开发）
├── docs/                   # 需求文档、设计文档
└── sql/                    # 数据库初始化脚本
```

## 📄 License

MIT
