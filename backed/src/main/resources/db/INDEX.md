# 数据库文件清单

## 📁 文件位置

所有数据库相关文件位于：`backed/src/main/resources/db/`

## 📋 文件列表

### 1. schema.sql
- **用途**：数据库表结构创建脚本
- **内容**：创建13张表的完整SQL语句
- **执行顺序**：第一个执行
- **文件大小**：约268行

### 2. data.sql
- **用途**：模拟数据插入脚本
- **内容**：包含74条模拟数据记录
- **执行顺序**：第二个执行（在schema.sql之后）
- **文件大小**：约170行

### 3. verify_data.sql
- **用途**：数据验证脚本
- **内容**：10个检查项，验证数据完整性
- **执行时机**：数据导入后执行
- **文件大小**：约161行

### 4. init_database.bat
- **用途**：Windows批处理初始化脚本
- **功能**：自动执行schema.sql和data.sql
- **使用方式**：双击运行或命令行执行
- **文件大小**：约49行

### 5. README.md
- **用途**：数据库初始化说明文档
- **内容**：执行步骤、测试账号、注意事项
- **文件大小**：约101行

### 6. DATA_DICTIONARY.md
- **用途**：数据库字典文档
- **内容**：详细的表结构、字段说明、枚举值
- **文件大小**：约440行

## 🚀 快速开始

### 方法一：使用批处理脚本（推荐）

```bash
# 进入db目录
cd D:\IdeaProjects\lab-equipment-management\backed\src\main\resources\db

# 执行批处理脚本
init_database.bat
```

### 方法二：手动执行SQL

```bash
# 1. 创建表结构
mysql -u root -p lab-equipment-management < schema.sql

# 2. 插入模拟数据
mysql -u root -p lab-equipment-management < data.sql

# 3. 验证数据（可选）
mysql -u root -p lab-equipment-management < verify_data.sql
```

### 方法三：使用数据库工具

1. 打开Navicat/MySQL Workbench
2. 连接到 `lab-equipment-management` 数据库
3. 依次执行：
   - schema.sql
   - data.sql
   - verify_data.sql（可选）

## 📊 数据统计

| 表名 | 记录数 | 说明 |
|------|--------|------|
| student | 10 | 学生信息（5个生物+5个化学） |
| teacher | 3 | 教师信息（1管理员+2教师） |
| device_category | 10 | 设备分类（2一级+8二级） |
| device | 15 | 设备信息（7生物+8化学） |
| device_image | 17 | 设备图片 |
| reservation | 8 | 预约记录 |
| borrow_record | 8 | 借用记录 |
| violation | 5 | 违规记录 |
| repair_record | 3 | 维修记录 |
| scrap_record | 1 | 报废记录 |
| announcement | 5 | 公告信息 |
| notification | 10 | 通知消息 |
| system_setting | 4 | 系统配置 |
| **总计** | **89** | **所有记录** |

## 👥 测试账号

### 学生账号（密码：123456）

| 学号 | 姓名 | 实验室 | 班级 | 状态 |
|------|------|--------|------|------|
| 2024001 | 张三 | bio | 生物技术1班 | 正常 |
| 2024002 | 李四 | bio | 生物技术1班 | 1次违规 |
| 2024003 | 王五 | bio | 生物技术2班 | 正常 |
| 2024004 | 赵六 | chem | 化学工程1班 | 正常 |
| 2024005 | 孙七 | chem | 化学工程1班 | 2次违规 |
| 2024006 | 周八 | chem | 化学工程2班 | 正常 |
| 2024007 | 吴九 | bio | 生物技术1班 | **已禁用** |
| 2024008 | 郑十 | bio | 生物技术2班 | 正常 |
| 2024009 | 陈一 | chem | 化学工程1班 | 正常 |
| 2024010 | 林二 | chem | 化学工程2班 | 正常 |

### 老师账号（密码：123456）

| 工号 | 姓名 | 角色 | 说明 |
|------|------|------|------|
| T001 | 李老师 | admin | 管理员 |
| T002 | 王老师 | teacher | 普通教师 |
| T003 | 张老师 | teacher | 普通教师 |

## 🔍 数据特点

### 设备状态分布
- available（可借用）：12台（80%）
- borrowed（已借出）：2台（13.3%）
- repair（维修中）：1台（6.7%）
- scrap（报废）：0台（0%）

### 预约状态分布
- pending（待审核）：3条
- approved（已通过）：3条
- rejected（被拒绝）：1条
- cancelled（已取消）：1条

### 借用记录分布
- borrowed（借用中）：2条
- returned（已归还）：5条
- overdue（已超时）：1条

### 违规类型分布
- overdue（超时）：3次
- damage（损坏）：1次
- other（其他）：1次

## ⚠️ 注意事项

1. **执行顺序**：必须先执行schema.sql，再执行data.sql
2. **字符集**：确保使用utf8mb4字符集
3. **外键检查**：脚本中已关闭外键检查（SET FOREIGN_KEY_CHECKS = 0）
4. **密码加密**：所有密码使用BCrypt加密，明文为"123456"
5. **时间数据**：模拟数据时间集中在2026年1月
6. **图片路径**：图片URL为相对路径，需配合前端资源使用

## 🛠️ 常见问题

### Q1: 执行时提示"Access denied"
**A**: 检查MySQL用户名和密码是否正确，确保有操作权限

### Q2: 提示"Database doesn't exist"
**A**: 先创建数据库：`CREATE DATABASE \`lab-equipment-management\` CHARACTER SET utf8mb4;`

### Q3: 中文显示乱码
**A**: 确保：
- 数据库字符集为utf8mb4
- 客户端字符集设置为utf8mb4
- SQL文件保存为UTF-8编码

### Q4: 外键约束错误
**A**: 确保按正确顺序执行：先schema.sql，再data.sql

### Q5: 如何清空数据重新导入
**A**: 
```sql
DROP DATABASE \`lab-equipment-management\`;
CREATE DATABASE \`lab-equipment-management\` CHARACTER SET utf8mb4;
-- 然后重新执行schema.sql和data.sql
```

## 📞 技术支持

如有问题，请查看：
- README.md - 详细使用说明
- DATA_DICTIONARY.md - 完整数据字典
- verify_data.sql - 数据验证工具

---

**最后更新**：2026-01-19  
**版本**：v1.0.0
