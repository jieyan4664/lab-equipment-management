# 数据库字典

## 表概览

| 序号 | 表名 | 中文名 | 记录数 | 说明 |
|------|------|--------|--------|------|
| 1 | student | 学生表 | 10 | 存储学生基本信息 |
| 2 | teacher | 老师表 | 3 | 存储老师/管理员信息 |
| 3 | device_category | 设备分类表 | 10 | 设备分类层级结构 |
| 4 | device | 设备表 | 15 | 设备详细信息 |
| 5 | device_image | 设备图片表 | 17 | 设备图片关联 |
| 6 | reservation | 预约表 | 8 | 设备预约记录 |
| 7 | borrow_record | 借用记录表 | 8 | 设备借用归还记录 |
| 8 | violation | 违规记录表 | 5 | 学生违规记录 |
| 9 | repair_record | 维修记录表 | 3 | 设备维修记录 |
| 10 | scrap_record | 报废记录表 | 1 | 设备报废记录 |
| 11 | announcement | 公告表 | 5 | 系统公告通知 |
| 12 | notification | 通知记录表 | 10 | 用户消息通知 |
| 13 | system_setting | 系统设置表 | 4 | 系统配置参数 |

---

## 1. student (学生表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| student_no | VARCHAR(20) | 学号（唯一） | 2024001 |
| name | VARCHAR(50) | 姓名 | 张三 |
| class_name | VARCHAR(50) | 班级 | 生物技术1班 |
| phone | VARCHAR(11) | 联系电话 | 13900139001 |
| email | VARCHAR(100) | 邮箱 | zhangsan@example.com |
| password | VARCHAR(255) | 密码（BCrypt加密） | $2a$10$... |
| lab_type | ENUM('bio','chem') | 实验室类型 | bio |
| access_status | TINYINT | 准入状态：1正常 2禁用 | 1 |
| access_expire | DATE | 准入有效期 | 2026-12-31 |
| violation_count | INT | 违规次数 | 0 |
| status | TINYINT | 账户状态：1正常 0禁用 | 1 |
| created_at | DATETIME | 创建时间 | 2026-01-01 00:00:00 |
| updated_at | DATETIME | 更新时间 | 2026-01-15 10:30:00 |

### 枚举值说明

**lab_type**: 
- `bio` - 生物实验室
- `chem` - 化学实验室

**access_status**: 
- `1` - 正常
- `2` - 禁用

---

## 2. teacher (老师表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| teacher_no | VARCHAR(20) | 工号（唯一） | T001 |
| name | VARCHAR(50) | 姓名 | 李老师 |
| phone | VARCHAR(11) | 联系电话 | 13800138001 |
| email | VARCHAR(100) | 邮箱 | li@example.com |
| password | VARCHAR(255) | 密码（BCrypt加密） | $2a$10$... |
| role | VARCHAR(20) | 角色 | admin |
| status | TINYINT | 账户状态：1正常 0禁用 | 1 |
| created_at | DATETIME | 创建时间 | 2026-01-01 00:00:00 |
| updated_at | DATETIME | 更新时间 | 2026-01-15 10:30:00 |

### 枚举值说明

**role**: 
- `teacher` - 普通教师
- `admin` - 管理员

---

## 3. device_category (设备分类表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| name | VARCHAR(50) | 分类名称 | 生物设备 |
| parent_id | INT | 父级ID（0为顶级） | 0 |
| lab_type | ENUM('bio','chem') | 所属实验室 | bio |
| sort_order | INT | 排序号 | 1 |
| status | TINYINT | 状态：1启用 0禁用 | 1 |
| created_at | DATETIME | 创建时间 | 2026-01-01 00:00:00 |

### 分类层级示例

```
生物设备 (parent_id=0)
├── 显微镜 (parent_id=1)
├── 离心机 (parent_id=1)
├── 培养箱 (parent_id=1)
└── 分光光度计 (parent_id=1)

化学设备 (parent_id=0)
├── 反应釜 (parent_id=2)
├── 滴定仪 (parent_id=2)
├── 色谱仪 (parent_id=2)
└── pH计 (parent_id=2)
```

---

## 4. device (设备表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| code | VARCHAR(50) | 设备编号（唯一） | DEV-BIO-001 |
| name | VARCHAR(100) | 设备名称 | 光学显微镜 |
| category_id | INT | 分类ID | 3 |
| brand | VARCHAR(50) | 品牌 | 奥林巴斯 |
| model | VARCHAR(100) | 型号 | CX23 |
| spec | TEXT | 规格参数 | 40x-1000x |
| technical_params | TEXT | 技术参数 | LED光源，双目镜筒 |
| location | VARCHAR(100) | 存放位置 | A栋-201-1号柜 |
| purchase_date | DATE | 购入日期 | 2024-03-15 |
| warranty_date | DATE | 保修截止日期 | 2026-03-15 |
| status | ENUM | 状态 | available |
| current_borrower_id | INT | 当前借用人ID | NULL |
| expected_return_time | DATETIME | 预计归还时间 | NULL |
| description | TEXT | 使用说明 | 用于细胞观察和微生物研究 |
| qr_code | VARCHAR(255) | 二维码标识 | QR-DEV-BIO-001 |
| created_at | DATETIME | 创建时间 | 2026-01-01 00:00:00 |
| updated_at | DATETIME | 更新时间 | 2026-01-15 10:30:00 |

### 枚举值说明

**status**: 
- `available` - 可借用
- `borrowed` - 已借出
- `repair` - 维修中
- `scrap` - 报废

---

## 5. device_image (设备图片表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| device_id | INT | 设备ID | 1 |
| image_url | VARCHAR(255) | 图片URL | /images/devices/microscope_1.jpg |
| sort_order | INT | 排序号 | 1 |
| created_at | DATETIME | 创建时间 | 2026-01-01 00:00:00 |

---

## 6. reservation (预约表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| student_id | INT | 学生ID | 1 |
| device_id | INT | 设备ID | 1 |
| start_time | DATETIME | 预约开始时间 | 2026-01-20 08:00:00 |
| end_time | DATETIME | 预约结束时间 | 2026-01-20 12:00:00 |
| purpose | VARCHAR(255) | 用途说明 | 细胞观察实验 |
| status | ENUM | 状态 | approved |
| reason | VARCHAR(255) | 拒绝/取消原因 | NULL |
| teacher_id | INT | 审核老师ID | 1 |
| audit_time | DATETIME | 审核时间 | 2026-01-15 14:30:00 |
| created_at | DATETIME | 创建时间 | 2026-01-15 10:30:00 |
| updated_at | DATETIME | 更新时间 | 2026-01-15 14:30:00 |

### 枚举值说明

**status**: 
- `pending` - 待审核
- `approved` - 已通过
- `rejected` - 被拒绝
- `cancelled` - 已取消
- `extending` - 延期申请中

---

## 7. borrow_record (借用记录表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| student_id | INT | 学生ID | 1 |
| device_id | INT | 设备ID | 3 |
| teacher_id | INT | 登记老师ID | 1 |
| borrow_time | DATETIME | 借用时间 | 2026-01-10 09:00:00 |
| due_time | DATETIME | 应还时间 | 2026-01-13 09:00:00 |
| return_time | DATETIME | 实际归还时间 | 2026-01-12 16:00:00 |
| status | ENUM | 状态 | returned |
| equipment_condition | ENUM | 归还时状态 | good |
| is_overdue | TINYINT | 是否超时：0否 1是 | 0 |
| remark | VARCHAR(255) | 备注 | 按时归还 |
| created_at | DATETIME | 创建时间 | 2026-01-10 09:00:00 |
| updated_at | DATETIME | 更新时间 | 2026-01-12 16:00:00 |

### 枚举值说明

**status**: 
- `borrowed` - 借用中
- `returned` - 已归还
- `overdue` - 已超时

**equipment_condition**: 
- `good` - 良好
- `worn` - 磨损
- `damaged` - 损坏
- `clean` - 已清洁

---

## 8. violation (违规记录表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| student_id | INT | 学生ID | 2 |
| borrow_id | INT | 关联借用记录ID | 2 |
| type | ENUM | 违规类型 | overdue |
| violation_time | DATETIME | 违规时间 | 2026-01-13 14:00:00 |
| punishment | ENUM | 处罚类型 | warning |
| ban_days | INT | 禁借用天数 | NULL |
| compensation_amount | DECIMAL(10,2) | 赔偿金额 | NULL |
| description | VARCHAR(500) | 违规说明 | 超时归还设备 |
| teacher_id | INT | 处理老师ID | 2 |
| status | TINYINT | 状态：1有效 0已撤销 | 1 |
| created_at | DATETIME | 创建时间 | 2026-01-13 14:00:00 |

### 枚举值说明

**type**: 
- `overdue` - 超时
- `damage` - 损坏
- `other` - 其他

**punishment**: 
- `warning` - 警告
- `ban` - 禁用
- `compensation` - 赔偿

---

## 9. repair_record (维修记录表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| device_id | INT | 设备ID | 6 |
| repair_date | DATE | 维修日期 | 2026-01-15 |
| repair_person | VARCHAR(50) | 维修人员 | 刘工程师 |
| cost | DECIMAL(10,2) | 维修费用 | 1500.00 |
| result | ENUM | 维修结果 | repaired |
| description | VARCHAR(500) | 维修说明 | 温控系统故障，更换温控模块 |
| images | TEXT | 维修凭证图片（JSON数组） | ["url1", "url2"] |
| teacher_id | INT | 登记老师ID | 1 |
| created_at | DATETIME | 创建时间 | 2026-01-15 10:00:00 |

### 枚举值说明

**result**: 
- `repaired` - 已修复
- `unrepairable` - 无法修复

---

## 10. scrap_record (报废记录表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| device_id | INT | 设备ID | NULL |
| scrap_date | DATE | 报废日期 | 2025-11-20 |
| reason | ENUM | 报废原因 | wear |
| description | VARCHAR(500) | 详细说明 | 老旧pH计，使用年限超过10年 |
| disposal | ENUM | 处置方式 | discard |
| teacher_id | INT | 登记老师ID | 1 |
| created_at | DATETIME | 创建时间 | 2025-11-20 10:00:00 |

### 枚举值说明

**reason**: 
- `wear` - 磨损老化
- `damage` - 损坏
- `obsolete` - 淘汰
- `other` - 其他

**disposal**: 
- `keep` - 保留
- `discard` - 丢弃
- `recycle` - 回收

---

## 11. announcement (公告表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| title | VARCHAR(100) | 标题 | 实验室开放时间调整通知 |
| content | TEXT | 内容（富文本HTML） | \<p\>各位同学...\</p\> |
| attachments | TEXT | 附件（JSON数组） | ["url1", "url2"] |
| target_type | VARCHAR(20) | 发布范围 | all |
| target_ids | TEXT | 目标ID列表（JSON数组） | NULL |
| is_pinned | TINYINT | 是否置顶 | 1 |
| publish_time | DATETIME | 发布时间 | 2026-01-10 09:00:00 |
| teacher_id | INT | 发布老师ID | 1 |
| status | TINYINT | 状态：1正常 0已删除 | 1 |
| created_at | DATETIME | 创建时间 | 2026-01-10 09:00:00 |

### 枚举值说明

**target_type**: 
- `all` - 所有人
- `bio` - 生物实验室
- `chem` - 化学实验室
- `class` - 指定班级
- `student` - 指定学生

---

## 12. notification (通知记录表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| user_id | INT | 接收用户ID | 1 |
| user_type | ENUM | 用户类型 | student |
| title | VARCHAR(100) | 通知标题 | 预约审核结果 |
| content | VARCHAR(500) | 通知内容 | 您的显微镜预约已通过审核 |
| type | VARCHAR(30) | 通知类型 | reservation |
| link | VARCHAR(255) | 跳转链接 | /student/reservations |
| is_read | TINYINT | 是否已读：0未读 1已读 | 0 |
| created_at | DATETIME | 创建时间 | 2026-01-15 14:30:00 |

### 枚举值说明

**user_type**: 
- `student` - 学生
- `teacher` - 老师

**type**: 
- `reservation` - 预约相关
- `borrow` - 借用相关
- `announcement` - 公告
- `system` - 系统通知

---

## 13. system_setting (系统设置表)

### 字段说明

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | INT | 主键ID | 1 |
| setting_key | VARCHAR(50) | 设置键（唯一） | lab_info |
| setting_value | TEXT | 设置值（JSON格式） | {"name":"..."} |
| description | VARCHAR(255) | 描述 | 实验室基本信息 |
| updated_at | DATETIME | 更新时间 | 2026-01-15 10:30:00 |

### 配置项说明

**setting_key**: 
- `lab_info` - 实验室基本信息
- `reservation_rules` - 预约规则配置
- `reminder_settings` - 归还提醒配置
- `system_version` - 系统版本号

---

## 索引说明

### 主要索引

| 表名 | 索引名 | 字段 | 类型 | 说明 |
|------|--------|------|------|------|
| student | uk_student_no | student_no | UNIQUE | 学号唯一索引 |
| teacher | uk_teacher_no | teacher_no | UNIQUE | 工号唯一索引 |
| device | uk_device_code | code | UNIQUE | 设备编号唯一索引 |
| device | idx_category_id | category_id | INDEX | 分类ID索引 |
| device | idx_status | status | INDEX | 状态索引 |
| reservation | idx_student_id | student_id | INDEX | 学生ID索引 |
| reservation | idx_device_id | device_id | INDEX | 设备ID索引 |
| reservation | idx_status | status | INDEX | 状态索引 |
| borrow_record | idx_student_id | student_id | INDEX | 学生ID索引 |
| borrow_record | idx_device_id | device_id | INDEX | 设备ID索引 |
| borrow_record | idx_status | status | INDEX | 状态索引 |
| violation | idx_student_id | student_id | INDEX | 学生ID索引 |
| violation | idx_borrow_id | borrow_id | INDEX | 借用记录ID索引 |
| notification | idx_user_id | user_id | INDEX | 用户ID索引 |
| notification | idx_is_read | is_read | INDEX | 已读状态索引 |

---

## 外键关系

虽然SQL脚本中未显式创建外键约束（为了提高性能），但逻辑上存在以下关系：

- `device.category_id` → `device_category.id`
- `device_image.device_id` → `device.id`
- `reservation.student_id` → `student.id`
- `reservation.device_id` → `device.id`
- `reservation.teacher_id` → `teacher.id`
- `borrow_record.student_id` → `student.id`
- `borrow_record.device_id` → `device.id`
- `borrow_record.teacher_id` → `teacher.id`
- `violation.student_id` → `student.id`
- `violation.borrow_id` → `borrow_record.id`
- `violation.teacher_id` → `teacher.id`
- `repair_record.device_id` → `device.id`
- `repair_record.teacher_id` → `teacher.id`
- `scrap_record.device_id` → `device.id`
- `scrap_record.teacher_id` → `teacher.id`
- `announcement.teacher_id` → `teacher.id`
