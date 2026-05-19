# 数据库关系图 (ER Diagram)

## 实体关系总览

```
┌──────────────┐       ┌──────────────┐       ┌──────────────────┐
│   student    │       │   teacher    │       │ device_category  │
├──────────────┤       ├──────────────┤       ├──────────────────┤
│ id (PK)      │       │ id (PK)      │       │ id (PK)          │
│ student_no   │       │ teacher_no   │       │ name             │
│ name         │       │ name         │       │ parent_id        │
│ class_name   │       │ phone        │       │ lab_type         │
│ phone        │       │ email        │       └────────┬─────────┘
│ email        │       │ password     │                │
│ password     │       │ role         │                │ 1:N
│ lab_type     │       │ status       │                │
│ access_status│       └──────┬───────┘       ┌────────▼─────────┐
│ violation_ct │              │               │     device       │
│ status       │              │ 1:N           ├──────────────────┤
└──────┬───────┘              │               │ id (PK)          │
       │ 1:N                  │               │ code             │
       │                      │               │ name             │
       │                      │               │ category_id (FK) │
       │                      │               │ brand, model     │
       │                      │               │ status           │
       │                      │               └────────┬─────────┘
       │                      │                        │ 1:N
       │                      │                        │
       ▼                      ▼                        ▼
┌──────────────┐       ┌──────────────┐       ┌──────────────────┐
│ reservation  │       │borrow_record │       │  device_image    │
├──────────────┤       ├──────────────┤       ├──────────────────┤
│ id (PK)      │       │ id (PK)      │       │ id (PK)          │
│ student(FK)  │       │ student(FK)  │       │ device_id (FK)   │
│ device (FK)  │       │ device (FK)  │       │ image_url        │
│ start_time   │       │ teacher(FK)  │       │ sort_order       │
│ end_time     │       │ borrow_time  │       └──────────────────┘
│ purpose      │       │ due_time     │
│ status       │       │ return_time  │
│ teacher(FK)  │       │ status       │
└──────────────┘       └──────┬───────┘
                              │ 1:N
                              │
                              ▼
                     ┌──────────────────┐
                     │   violation      │
                     ├──────────────────┤
                     │ id (PK)          │
                     │ student_id (FK)  │
                     │ borrow_id (FK)   │
                     │ type             │
                     │ punishment       │
                     │ teacher_id (FK)  │
                     └──────────────────┘


┌──────────────┐       ┌──────────────┐       ┌──────────────────┐
│repair_record │       │scrap_record  │       │ announcement     │
├──────────────┤       ├──────────────┤       ├──────────────────┤
│ id (PK)      │       │ id (PK)      │       │ id (PK)          │
│ device (FK)  │       │ device (FK)  │       │ title            │
│ repair_date  │       │ scrap_date   │       │ content          │
│ cost         │       │ reason       │       │ target_type      │
│ teacher(FK)  │       │ teacher(FK)  │       │ teacher_id (FK)  │
└──────────────┘       └──────────────┘       └──────────────────┘


┌──────────────┐       ┌──────────────────┐
│notification  │       │system_setting    │
├──────────────┤       ├──────────────────┤
│ id (PK)      │       │ id (PK)          │
│ user_id      │       │ setting_key      │
│ user_type    │       │ setting_value    │
│ title        │       └──────────────────┘
│ content      │
│ type         │
│ is_read      │
└──────────────┘
```

## 主要关系说明

### 1. 用户相关关系

#### Student（学生）相关
- **student** 1:N **reservation** - 一个学生可以提交多个预约
- **student** 1:N **borrow_record** - 一个学生可以有多条借用记录
- **student** 1:N **violation** - 一个学生可以有多条违规记录
- **student** 1:N **notification** - 一个学生可以接收多条通知

#### Teacher（老师）相关
- **teacher** 1:N **reservation** - 一个老师可以审核多个预约
- **teacher** 1:N **borrow_record** - 一个老师可以登记多个借用
- **teacher** 1:N **violation** - 一个老师可以处理多个违规
- **teacher** 1:N **repair_record** - 一个老师可以登记多个维修
- **teacher** 1:N **scrap_record** - 一个老师可以登记多个报废
- **teacher** 1:N **announcement** - 一个老师可以发布多个公告

### 2. 设备相关关系

#### Device（设备）核心关系
- **device_category** 1:N **device** - 一个分类下有多个设备
- **device** 1:N **device_image** - 一个设备可以有多张图片
- **device** 1:N **reservation** - 一个设备可以被多次预约
- **device** 1:N **borrow_record** - 一个设备可以有多次借用记录
- **device** 1:N **repair_record** - 一个设备可以有多次维修记录
- **device** 1:N **scrap_record** - 一个设备可以有报废记录

#### 设备状态流转
```
available → borrowed → returned
    ↓
  repair → repaired → available
    ↓
  scrap (报废)
```

### 3. 业务流程关系

#### 预约流程
```
student 提交预约 → reservation (pending)
                      ↓
                 teacher 审核
                      ↓
            approved / rejected
```

#### 借用流程
```
reservation (approved) → borrow_record (borrowed)
                              ↓
                         归还设备
                              ↓
                    borrow_record (returned)
                              ↓
                    检查是否有违规 → violation
```

#### 违规处理
```
borrow_record (overdue/damaged)
        ↓
   violation 记录
        ↓
   处罚：warning/ban/compensation
```

### 4. 数据关联示例

#### 完整的借用流程数据链
```sql
-- 1. 学生信息
SELECT * FROM student WHERE id = 1;

-- 2. 预约记录
SELECT * FROM reservation WHERE student_id = 1 AND device_id = 3;

-- 3. 借用记录
SELECT * FROM borrow_record WHERE student_id = 1 AND device_id = 3;

-- 4. 如有违规
SELECT * FROM violation WHERE student_id = 1 AND borrow_id = [借用记录ID];

-- 5. 相关通知
SELECT * FROM notification WHERE user_id = 1 AND user_type = 'student';
```

#### 设备完整信息查询
```sql
-- 设备基本信息
SELECT * FROM device WHERE id = 1;

-- 设备图片
SELECT * FROM device_image WHERE device_id = 1 ORDER BY sort_order;

-- 设备分类
SELECT * FROM device_category WHERE id = [category_id];

-- 设备预约历史
SELECT * FROM reservation WHERE device_id = 1;

-- 设备借用历史
SELECT * FROM borrow_record WHERE device_id = 1;

-- 设备维修历史
SELECT * FROM repair_record WHERE device_id = 1;
```

## 关键外键关系表

| 从表 | 字段 | 引用表 | 字段 | 关系 |
|------|------|--------|------|------|
| device | category_id | device_category | id | N:1 |
| device_image | device_id | device | id | N:1 |
| reservation | student_id | student | id | N:1 |
| reservation | device_id | device | id | N:1 |
| reservation | teacher_id | teacher | id | N:1 |
| borrow_record | student_id | student | id | N:1 |
| borrow_record | device_id | device | id | N:1 |
| borrow_record | teacher_id | teacher | id | N:1 |
| violation | student_id | student | id | N:1 |
| violation | borrow_id | borrow_record | id | N:1 |
| violation | teacher_id | teacher | id | N:1 |
| repair_record | device_id | device | id | N:1 |
| repair_record | teacher_id | teacher | id | N:1 |
| scrap_record | device_id | device | id | N:1 |
| scrap_record | teacher_id | teacher | id | N:1 |
| announcement | teacher_id | teacher | id | N:1 |

## 索引策略

### 主键索引
- 所有表的 `id` 字段都建立了主键索引

### 唯一索引
- `student.student_no` - 学号唯一
- `teacher.teacher_no` - 工号唯一
- `device.code` - 设备编号唯一
- `system_setting.setting_key` - 设置键唯一

### 普通索引
- `device.category_id` - 加速按分类查询
- `device.status` - 加速按状态筛选
- `reservation.student_id` - 加速查询学生预约
- `reservation.device_id` - 加速查询设备预约
- `reservation.status` - 加速按状态筛选
- `borrow_record.student_id` - 加速查询学生借用
- `borrow_record.device_id` - 加速查询设备借用
- `borrow_record.status` - 加速按状态筛选
- `violation.student_id` - 加速查询学生违规
- `violation.borrow_id` - 加速关联借用记录
- `notification.user_id` - 加速查询用户通知
- `notification.is_read` - 加速筛选未读消息

## 性能优化建议

1. **分页查询**: 对于列表查询，务必使用 LIMIT 和 OFFSET
2. **索引覆盖**: 常用查询条件应建立联合索引
3. **避免N+1**: 使用 JOIN 或批量查询减少数据库访问次数
4. **缓存热点数据**: 如系统设置、设备分类等不常变化的数据
5. **定期清理**: 历史通知、已取消的预约等可定期归档

---

**提示**: 虽然SQL脚本中未显式创建FOREIGN KEY约束，但应用程序层面应维护这些关系的完整性。
