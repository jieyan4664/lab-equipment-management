# ⚡ 数据库初始化 - 快速指南

## 🎯 一键初始化（推荐）

```bash
# 双击运行或在命令行执行
D:\IdeaProjects\lab-equipment-management\backed\src\main\resources\db\init_database.bat
```

## 📝 手动执行

```bash
# 步骤1: 创建表结构
mysql -u root -p lab-equipment-management < schema.sql

# 步骤2: 插入数据
mysql -u root -p lab-equipment-management < data.sql

# 步骤3: 验证数据（可选）
mysql -u root -p lab-equipment-management < verify_data.sql
```

## 🔑 测试账号

**学生**: 学号 `2024001` ~ `2024010`，密码 `123456`  
**老师**: 工号 `T001` ~ `T003`，密码 `123456`

## 📊 数据概览

- ✅ 13张表已创建
- ✅ 89条模拟数据
- ✅ 10个学生 + 3个老师
- ✅ 15台设备（7生物+8化学）
- ✅ 完整的业务数据（预约、借用、违规等）

## 📚 文档索引

| 文档 | 说明 |
|------|------|
| [INDEX.md](./INDEX.md) | 📋 文件清单总览 |
| [README.md](./README.md) | 📖 详细使用说明 |
| [DATA_DICTIONARY.md](./DATA_DICTIONARY.md) | 📊 完整数据字典 |

## ❓ 遇到问题？

1. 检查数据库是否已创建：`CREATE DATABASE \`lab-equipment-management\`;`
2. 检查MySQL服务是否启动
3. 查看详细文档：[README.md](./README.md)

---
**提示**: 建议使用 `init_database.bat` 一键完成初始化！
