# 数据库初始化说明

## 文件说明

- `schema.sql` - 数据库表结构创建脚本（13张表）
- `data.sql` - 模拟数据插入脚本

## 执行步骤

### 方法一：使用MySQL命令行

1. 打开命令行工具，进入SQL文件所在目录：
```bash
cd D:\IdeaProjects\lab-equipment-management\backed\src\main\resources\db
```

2. 执行表结构创建脚本：
```bash
mysql -u root -p lab-equipment-management < schema.sql
```

3. 执行模拟数据插入脚本：
```bash
mysql -u root -p lab-equipment-management < data.sql
```

### 方法二：使用MySQL客户端工具

1. 打开Navicat、MySQL Workbench或其他数据库管理工具
2. 连接到 `lab-equipment-management` 数据库
3. 依次执行以下操作：
   - 打开 `schema.sql` 文件并执行
   - 打开 `data.sql` 文件并执行

### 方法三：在MySQL命令行中直接执行

```sql
-- 登录MySQL
mysql -u root -p

-- 选择数据库
USE `lab-equipment-management`;

-- 执行表结构脚本
SOURCE D:/IdeaProjects/lab-equipment-management/backed/src/main/resources/db/schema.sql;

-- 执行数据脚本
SOURCE D:/IdeaProjects/lab-equipment-management/backed/src/main/resources/db/data.sql;
```

## 数据库表说明

系统共包含13张表：

1. **student** - 学生表（10条模拟数据）
2. **teacher** - 老师表（3条模拟数据）
3. **device_category** - 设备分类表（10条模拟数据）
4. **device** - 设备表（15条模拟数据）
5. **device_image** - 设备图片表（17条模拟数据）
6. **reservation** - 预约表（8条模拟数据）
7. **borrow_record** - 借用记录表（8条模拟数据）
8. **violation** - 违规记录表（5条模拟数据）
9. **repair_record** - 维修记录表（3条模拟数据）
10. **scrap_record** - 报废记录表（1条模拟数据）
11. **announcement** - 公告表（5条模拟数据）
12. **notification** - 通知记录表（10条模拟数据）
13. **system_setting** - 系统设置表（4条模拟数据）

## 测试账号信息

所有密码统一为：`123456`（BCrypt加密后的值为：`$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`）

### 学生账号
| 学号 | 姓名 | 实验室类型 | 班级 |
|------|------|-----------|------|
| 2024001 | 张三 | bio | 生物技术1班 |
| 2024002 | 李四 | bio | 生物技术1班 |
| 2024003 | 王五 | bio | 生物技术2班 |
| 2024004 | 赵六 | chem | 化学工程1班 |
| 2024005 | 孙七 | chem | 化学工程1班 |
| 2024006 | 周八 | chem | 化学工程2班 |
| 2024007 | 吴九 | bio | 生物技术1班（已禁用） |
| 2024008 | 郑十 | bio | 生物技术2班 |
| 2024009 | 陈一 | chem | 化学工程1班 |
| 2024010 | 林二 | chem | 化学工程2班 |

### 老师账号
| 工号 | 姓名 | 角色 |
|------|------|------|
| T001 | 李老师 | admin |
| T002 | 王老师 | teacher |
| T003 | 张老师 | teacher |

## 注意事项

1. 执行前请确保已创建 `lab-equipment-management` 数据库
2. 如需重新初始化，先执行 `DROP DATABASE` 再重新创建
3. 模拟数据中的时间均为2026年1月的日期
4. 设备状态包括：available（可借用）、borrowed（已借出）、repair（维修中）、scrap（报废）
5. 部分学生有违规记录，可用于测试违规相关功能
