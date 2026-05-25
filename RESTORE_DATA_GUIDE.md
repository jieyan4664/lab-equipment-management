# 📋 数据库恢复步骤

## ⚠️ 当前状态

您已经执行了 `schema.sql`，这意味着：
- ✅ 所有表结构已创建（包括新的 `announcement_read` 表）
- ❌ 所有测试数据已丢失

---

## 🔧 恢复步骤

### 方法1：MySQL命令行（推荐）

```bash
# 1. 连接MySQL
mysql -u root -p

# 2. 选择数据库
USE lab-equipment-management;

# 3. 执行数据脚本
SOURCE D:/IdeaProjects/lab-equipment-management/backed/src/main/resources/db/data.sql;

# 4. 验证数据是否插入成功
SELECT COUNT(*) FROM student;   -- 应该返回 10
SELECT COUNT(*) FROM teacher;   -- 应该返回 3
SELECT COUNT(*) FROM device;    -- 应该返回 15
```

---

### 方法2：IDEA数据库控制台

**步骤**：
1. 打开文件：`backed/src/main/resources/db/data.sql`
2. 按 `Ctrl+A` 全选
3. 按 `Ctrl+Enter` 执行
4. 查看输出确认执行成功

**注意**：不要在IDEA中使用 `SOURCE` 命令，直接执行SQL内容即可。

---

## ✅ 验证数据恢复

执行以下SQL检查数据是否正确：

```sql
-- 检查学生数据
SELECT id, name, student_no FROM student;

-- 检查老师数据
SELECT id, name, teacher_no FROM teacher;

-- 检查设备数据
SELECT id, name, status FROM device;

-- 检查公告数据
SELECT id, title, target_type FROM announcement;

-- 检查新表是否存在
DESCRIBE announcement_read;
```

---

## 🎯 测试登录

数据恢复后，可以使用以下账号测试登录：

### 学生账号
| 学号 | 姓名 | 密码 | 实验室类型 |
|------|------|------|-----------|
| 2024001 | 张三 | 123456 | bio |
| 2024002 | 李四 | 123456 | bio |
| 2024004 | 赵六 | 123456 | chem |
| 2024005 | 孙七 | 123456 | chem |

### 老师账号
| 工号 | 姓名 | 密码 | 角色 |
|------|------|------|------|
| T001 | 李老师 | 123456 | admin |
| T002 | 王老师 | 123456 | teacher |
| T003 | 张老师 | 123456 | teacher |

**默认密码**：`123456`（加密后的密码在数据库中）

---

## 🚀 重启后端服务

数据恢复后，重启后端服务：

```bash
cd backed
mvn clean package
java -jar target/backed-0.0.1-SNAPSHOT.jar
```

或者在IDEA中直接运行 `BackedApplication`。

---

## 📊 预期结果

访问 http://localhost:3000/student/dashboard

应该能看到：
- ✅ 欢迎横幅显示学生信息
- ✅ 统计卡片显示借用数和预约数
- ✅ 最新公告列表（根据实验室类型筛选）
- ✅ 快速入口按钮正常工作

---

## ❓ 常见问题

### Q: 执行data.sql时报错？

**A**: 检查错误信息，可能是：
1. 外键约束问题 → 确保先执行schema.sql再执行data.sql
2. 唯一键冲突 → 确保表中没有残留数据
3. 字段长度不足 → 检查SQL中的数据是否符合表结构

### Q: 登录后还是提示"用户不存在"？

**A**: 
1. 确认data.sql执行成功
2. 检查student表中是否有数据：`SELECT * FROM student;`
3. 重启后端服务
4. 清除浏览器缓存

### Q: announcement_read表在哪里？

**A**: 该表已在schema.sql中定义（第267-283行），执行schema.sql时已自动创建。

---

**最后更新**：2026-05-19  
**适用版本**：lab-equipment-management v1.0
