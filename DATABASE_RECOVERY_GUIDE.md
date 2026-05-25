# ⚠️ 数据库紧急恢复指南

## 🚨 问题说明

如果您执行了 `schema.sql` 脚本后出现"用户不存在"的500错误，是因为该脚本包含 `DROP TABLE IF EXISTS` 语句，会**删除所有现有表和数据**。

---

## ✅ 正确的操作步骤

### 方案1：使用增量SQL脚本（推荐）

**适用场景**：只需要添加新表，保留现有数据

```bash
# 1. 连接MySQL
mysql -u root -p

# 2. 选择数据库
USE lab-equipment-management;

# 3. 执行增量脚本（只添加announcement_read表）
SOURCE D:/IdeaProjects/lab-equipment-management/backed/src/main/resources/db/add_announcement_read_table.sql;

# 4. 验证表是否创建成功
SHOW TABLES LIKE 'announcement_read';
DESCRIBE announcement_read;
```

---

### 方案2：在IDEA中执行

**步骤**：
1. 打开文件：`backed/src/main/resources/db/add_announcement_read_table.sql`
2. 按 `Ctrl+A` 全选
3. 按 `Ctrl+Enter` 执行
4. 查看输出确认执行成功

---

## 🔍 验证数据是否丢失

```sql
-- 检查学生表是否有数据
SELECT COUNT(*) FROM student;

-- 检查老师表是否有数据
SELECT COUNT(*) FROM teacher;

-- 检查设备表是否有数据
SELECT COUNT(*) FROM device;
```

如果返回结果为 `0`，说明数据已丢失，需要重新插入测试数据。

---

## 📝 schema.sql 的正确用途

**schema.sql 是完整的数据库初始化脚本**，适用于：
- ✅ 全新项目首次部署
- ✅ 开发环境重置数据库
- ❌ **不适用于生产环境增量更新**

**重要提醒**：
- `schema.sql` 包含 `DROP TABLE IF EXISTS` 语句
- 执行后会删除所有现有表和數據
- 仅在全新安装或测试环境使用

---

## 🛡️ 未来避免此问题的最佳实践

### 1. 分离初始化和增量脚本

```
db/
├── schema.sql              # 完整初始化脚本（含DROP TABLE）
├── add_announcement_read_table.sql  # 增量脚本（仅CREATE TABLE）
└── migrations/             # 数据库迁移脚本目录
    ├── 001_create_xxx.sql
    ├── 002_add_yyy.sql
    └── ...
```

### 2. 使用 Flyway/Liquibase 管理数据库版本

自动跟踪和执行数据库变更，避免手动执行SQL。

### 3. 执行前备份数据

```bash
# 备份整个数据库
mysqldump -u root -p lab-equipment-management > backup_$(date +%Y%m%d).sql

# 恢复数据
mysql -u root -p lab-equipment-management < backup_20260519.sql
```

---

## 🎯 当前状态

- ✅ `schema.sql` 已恢复原状（不包含 announcement_read 表）
- ✅ 创建了独立的增量脚本 `add_announcement_read_table.sql`
- ✅ 后端代码已完成（AnnouncementRead实体、Mapper、Service）

---

## 📋 下一步操作

### 如果您还没有执行过 schema.sql

直接执行增量脚本即可：

```bash
mysql -u root -p
USE lab-equipment-management;
SOURCE D:/IdeaProjects/lab-equipment-management/backed/src/main/resources/db/add_announcement_read_table.sql;
```

### 如果您已经执行过 schema.sql 导致数据丢失

需要重新插入测试数据。请告诉我，我可以帮您生成测试数据SQL脚本。

---

## ❓ 常见问题

### Q: 我怎么知道数据是否丢失？

**A**: 刷新网页后出现以下错误之一：
- "用户不存在" (500错误)
- 登录失败
- 列表为空

### Q: 如何快速恢复？

**A**: 
1. 如果有备份：`mysql -u root -p lab-equipment-management < backup.sql`
2. 如果没有备份：需要重新插入测试数据

### Q: 以后添加新表怎么办？

**A**: 创建独立的增量SQL文件，例如：
- `add_xxx_table.sql`
- `alter_yyy_table.sql`
- 不要修改 `schema.sql`

---

**修复日期**：2026-05-19  
**影响范围**：仅数据库结构，不影响后端代码
