# 实验室设备仪器管理系统 - 完整设计文档

## 目录

1. [页面内容设计（Markdown格式）](#一页面内容设计markdown格式)
2. [数据库设计](#二数据库设计)
3. [接口文档](#三接口文档)

---

# 一、页面内容设计（Markdown格式）

## 1. 学生端

### 1.1 首页/仪表盘

| 模块 | 内容 |
|------|------|
| 欢迎横幅 | 显示学生姓名、学号、所属实验室（生物/化学） |
| 公告栏 | 最新3条公告，显示标题、发布时间、已读/未读状态 |
| 快速入口 | 设备查询、我的预约、我的借用 |
| 统计卡片 | 当前借用设备数、待处理预约数 |

### 1.2 设备查询与浏览

| 模块 | 内容 |
|------|------|
| 筛选栏 | 类别筛选（生物/化学）、状态筛选（可借用/维修中/已借出） |
| 搜索框 | 按设备名称/编号搜索，支持防抖和搜索建议 |
| 设备卡片 | 缩略图、名称、编号、型号、存放位置、状态、收藏按钮 |
| 分页 | 每页12条，滚动加载更多 |

### 1.3 设备详情页

| 模块 | 内容 |
|------|------|
| 基础信息 | 设备名称、编号、类别、品牌、型号、规格参数、技术参数、存放位置、购入日期、图片轮播 |
| 状态信息 | 当前状态、当前借用人、预计归还时间 |
| 可用时段 | 7天内日历，标记可用/已约/维修 |
| 预约表单 | 预约日期、开始时段、预计归还时间、用途说明、勾选须知 |
| 评论区 | 评论人、评分、评论内容、时间、点赞数 |

### 1.4 我的预约

| 模块 | 内容 |
|------|------|
| 当前预约 | 按状态分组（待审核/已通过/被拒绝/延期申请中），显示设备名、预约时间、操作按钮 |
| 历史预约 | 最近3个月记录，显示设备名、借还时间、违规标记、评价状态 |

### 1.5 我的借用记录

| 模块 | 内容 |
|------|------|
| 当前借用 | 设备名、借用时间、应还时间、剩余/超时天数、归还凭证码、申诉按钮 |
| 借用历史 | 设备名、借还时间、确认老师、设备归还状态、违规记录 |

### 1.6 个人中心

| 模块 | 内容 |
|------|------|
| 个人信息 | 姓名、学号、班级、联系电话、邮箱、实验室准入有效期 |
| 违规记录 | 违规时间、设备名、违规类型、处罚措施、处理老师 |
| 消息通知 | 标题、内容、类型、发送时间、已读状态、跳转链接 |

---

## 2. 老师端

### 2.1 首页/管理仪表盘

| 模块 | 内容 |
|------|------|
| 数据卡片 | 设备总数、可借用数、维修中数、今日预约数、待审核数、借用中数、超时数、活跃学生数、违规学生数 |
| 待办事项 | 超时催还、待审核预约、待维修，显示优先级和操作按钮 |
| 图表 | 借用TOP5设备、各类型使用率、月度借用趋势 |

### 2.2 设备管理

| 模块 | 内容 |
|------|------|
| 设备列表 | 表格：名称、编号、类别、型号、存放位置、购入日期、保修期、状态、操作 |
| 添加/编辑设备 | 表单：名称、编号、类别、品牌、型号、规格参数、技术参数、存放位置、购入日期、保修期、使用说明附件、图片 |
| 分类管理 | 分类名称、父级分类、排序号 |
| 批量操作 | 生成二维码标签、批量导入/导出 |

### 2.3 预约审核

| 模块 | 内容 |
|------|------|
| 待审核列表 | 申请时间、学生、设备、预约时间、用途、等待时长、通过/拒绝按钮 |
| 拒绝表单 | 拒绝理由 |
| 已审核列表 | 审核结果、审核人、审核时间 |
| 预约日历 | 按设备/按时间查看，颜色标记状态 |

### 2.4 借用/归还管理

| 模块 | 内容 |
|------|------|
| 借用登记 | 扫码或手动：设备信息、学生学号、学生状态、应还时间、备注 |
| 归还登记 | 扫码：借用记录、设备状态（正常/磨损/损坏/需清洁）、损坏拍照、超时标记、违规处理 |
| 当前借用列表 | 设备、学生、借用时间、应还时间、超时状态、操作（催还/标记丢失） |

### 2.5 学生管理

| 模块 | 内容 |
|------|------|
| 学生列表 | 表格：姓名、学号、班级、联系电话、准入状态、当前借用数、累计借用次数、违规次数、操作 |
| 编辑学生 | 班级、联系电话、邮箱、准入状态、禁用原因、禁用期限 |
| 违规记录管理 | 添加/编辑违规：学生、设备、违规类型、时间、处罚措施、处罚天数、赔偿金额、说明 |

### 2.6 维修/报废管理

| 模块 | 内容 |
|------|------|
| 待维修列表 | 设备、类别、存放位置、报修时间、报修人、故障描述、优先级 |
| 维修登记 | 维修日期、维修人员、维修费用、维修结果、说明、凭证 |
| 报废登记 | 报废设备、报废日期、报废原因、详细说明、处置方式 |

### 2.7 公告与通知管理

| 模块 | 内容 |
|------|------|
| 发布公告 | 标题、内容（富文本）、附件、发布范围、置顶、定时发布 |
| 定向通知 | 接收对象、通知模板、通知内容、发送方式（站内信/短信） |
| 通知记录 | 历史发送记录及状态 |

### 2.8 数据统计与报表

| 模块 | 内容 |
|------|------|
| 设备借用统计 | 设备排行（柱状图+表格）、类别占比（饼图）、月度趋势（折线图） |
| 学生活跃度 | 借用TOP学生、班级活跃度 |
| 违规统计 | 违规类型分布（饼图）、每月趋势（柱状图） |
| 手动报表生成 | 选择报表类型、周期、包含内容、导出格式（Excel/PDF） |

### 2.9 系统设置

| 模块 | 内容 |
|------|------|
| 实验室信息 | 名称、简介、开放时间、规则说明 |
| 预约规则 | 最大预约时长、提前预约天数、取消提前时间、最大借用数、时段粒度 |
| 归还提醒 | 超时阈值、催还方式、催还间隔、提前提醒 |
| 角色权限 | 角色名称、权限模块多选 |

---

## 3. 公共功能

| 模块 | 内容 |
|------|------|
| 登录 | 角色选择、账号、密码、验证码、记住密码 |
| 全局搜索 | 搜索结果：设备名、编号、类别、状态、高亮匹配 |
| 消息推送 | WebSocket实时推送，未读角标 |
| 表单校验 | 实时校验、提交前统一校验、toast反馈 |

---

# 二、数据库设计

## 2.1 ER图（实体关系图）

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    student  │     │   teacher   │     │   device    │
├─────────────┤     ├─────────────┤     ├─────────────┤
│ id (PK)     │     │ id (PK)     │     │ id (PK)     │
│ name        │     │ name        │     │ name        │
│ student_no  │     │ teacher_no  │     │ code        │
│ class       │     │ phone       │     │ category_id │
│ phone       │     │ email       │     │ brand       │
│ email       │     │ role        │     │ model       │
│ status      │     │ status      │     │ spec        │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       │                   │                   │
       ▼                   ▼                   ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ reservation │     │   borrow    │     │  category   │
├─────────────┤     ├─────────────┤     ├─────────────┤
│ id (PK)     │     │ id (PK)     │     │ id (PK)     │
│ student_id  │     │ student_id  │     │ name        │
│ device_id   │     │ device_id   │     │ parent_id   │
│ start_time  │     │ teacher_id  │     │ sort_order  │
│ end_time    │     │ borrow_time │     └─────────────┘
│ purpose     │     │ due_time    │
│ status      │     │ return_time │
│ reason      │     │ status      │
│ teacher_id  │     │ equipment_  │
│ audit_time  │     │ condition   │
└─────────────┘     └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  violation  │
                    ├─────────────┤
                    │ id (PK)     │
                    │ student_id  │
                    │ borrow_id   │
                    │ type        │
                    │ time        │
                    │ punishment  │
                    │ days        │
                    │ amount      │
                    │ description │
                    │ teacher_id  │
                    └─────────────┘
```

## 2.2 数据表结构

### 2.2.1 学生表 (student)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| student_no | VARCHAR | 20 | 否 | - | 学号（唯一） |
| name | VARCHAR | 50 | 否 | - | 姓名 |
| class_name | VARCHAR | 50 | 否 | - | 班级 |
| phone | VARCHAR | 11 | 是 | NULL | 联系电话 |
| email | VARCHAR | 100 | 是 | NULL | 邮箱 |
| password | VARCHAR | 255 | 否 | - | 密码（加密） |
| lab_type | ENUM | - | 否 | 'bio' | 实验室类型：bio/chem |
| access_status | TINYINT | - | 否 | 1 | 准入状态：1正常 2禁用 |
| access_expire | DATE | - | 是 | NULL | 准入有效期 |
| violation_count | INT | - | 否 | 0 | 违规次数 |
| status | TINYINT | - | 否 | 1 | 账户状态：1正常 0禁用 |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 更新时间 |

### 2.2.2 老师表 (teacher)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| teacher_no | VARCHAR | 20 | 否 | - | 工号（唯一） |
| name | VARCHAR | 50 | 否 | - | 姓名 |
| phone | VARCHAR | 11 | 是 | NULL | 联系电话 |
| email | VARCHAR | 100 | 是 | NULL | 邮箱 |
| password | VARCHAR | 255 | 否 | - | 密码（加密） |
| role | VARCHAR | 20 | 否 | 'teacher' | 角色：teacher/admin |
| status | TINYINT | - | 否 | 1 | 账户状态：1正常 0禁用 |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 更新时间 |

### 2.2.3 设备分类表 (device_category)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| name | VARCHAR | 50 | 否 | - | 分类名称 |
| parent_id | INT | - | 是 | 0 | 父级ID（0为顶级） |
| lab_type | ENUM | - | 是 | NULL | 所属实验室：bio/chem |
| sort_order | INT | - | 否 | 0 | 排序号 |
| status | TINYINT | - | 否 | 1 | 状态：1启用 0禁用 |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |

### 2.2.4 设备表 (device)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| code | VARCHAR | 50 | 否 | - | 设备编号（唯一） |
| name | VARCHAR | 100 | 否 | - | 设备名称 |
| category_id | INT | - | 否 | - | 分类ID（外键） |
| brand | VARCHAR | 50 | 是 | NULL | 品牌 |
| model | VARCHAR | 100 | 是 | NULL | 型号 |
| spec | TEXT | - | 是 | NULL | 规格参数 |
| technical_params | TEXT | - | 是 | NULL | 技术参数 |
| location | VARCHAR | 100 | 否 | - | 存放位置 |
| purchase_date | DATE | - | 是 | NULL | 购入日期 |
| warranty_date | DATE | - | 是 | NULL | 保修截止日期 |
| status | ENUM | - | 否 | 'available' | 状态：available/borrowed/repair/scrap |
| current_borrower_id | INT | - | 是 | NULL | 当前借用人ID |
| expected_return_time | DATETIME | - | 是 | NULL | 预计归还时间 |
| description | TEXT | - | 是 | NULL | 使用说明 |
| qr_code | VARCHAR | 255 | 是 | NULL | 二维码标识 |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 更新时间 |

### 2.2.5 设备图片表 (device_image)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| device_id | INT | - | 否 | - | 设备ID（外键） |
| image_url | VARCHAR | 255 | 否 | - | 图片URL |
| sort_order | INT | - | 否 | 0 | 排序号 |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |

### 2.2.6 预约表 (reservation)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| student_id | INT | - | 否 | - | 学生ID（外键） |
| device_id | INT | - | 否 | - | 设备ID（外键） |
| start_time | DATETIME | - | 否 | - | 预约开始时间 |
| end_time | DATETIME | - | 否 | - | 预约结束时间 |
| purpose | VARCHAR | 255 | 否 | - | 用途说明 |
| status | ENUM | - | 否 | 'pending' | 状态：pending/approved/rejected/cancelled/extending |
| reason | VARCHAR | 255 | 是 | NULL | 拒绝/取消原因 |
| teacher_id | INT | - | 是 | NULL | 审核老师ID |
| audit_time | DATETIME | - | 是 | NULL | 审核时间 |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 更新时间 |

### 2.2.7 借用记录表 (borrow_record)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| student_id | INT | - | 否 | - | 学生ID（外键） |
| device_id | INT | - | 否 | - | 设备ID（外键） |
| teacher_id | INT | - | 是 | NULL | 登记老师ID |
| borrow_time | DATETIME | - | 否 | CURRENT_TIMESTAMP | 借用时间 |
| due_time | DATETIME | - | 否 | - | 应还时间 |
| return_time | DATETIME | - | 是 | NULL | 实际归还时间 |
| status | ENUM | - | 否 | 'borrowed' | 状态：borrowed/returned/overdue |
| equipment_condition | ENUM | - | 是 | NULL | 归还时状态：good/worn/damaged/clean |
| is_overdue | TINYINT | - | 否 | 0 | 是否超时：0否 1是 |
| remark | VARCHAR | 255 | 是 | NULL | 备注 |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 更新时间 |

### 2.2.8 违规记录表 (violation)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| student_id | INT | - | 否 | - | 学生ID（外键） |
| borrow_id | INT | - | 是 | NULL | 关联借用记录ID |
| type | ENUM | - | 否 | - | 类型：overdue/damage/other |
| violation_time | DATETIME | - | 否 | CURRENT_TIMESTAMP | 违规时间 |
| punishment | ENUM | - | 否 | 'warning' | 处罚：warning/ban/compensation |
| ban_days | INT | - | 是 | NULL | 禁借用天数 |
| compensation_amount | DECIMAL | 10,2 | 是 | NULL | 赔偿金额 |
| description | VARCHAR | 500 | 否 | - | 违规说明 |
| teacher_id | INT | - | 否 | - | 处理老师ID |
| status | TINYINT | - | 否 | 1 | 状态：1有效 0已撤销 |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |

### 2.2.9 维修记录表 (repair_record)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| device_id | INT | - | 否 | - | 设备ID（外键） |
| repair_date | DATE | - | 否 | - | 维修日期 |
| repair_person | VARCHAR | 50 | 否 | - | 维修人员 |
| cost | DECIMAL | 10,2 | 是 | 0 | 维修费用 |
| result | ENUM | - | 否 | 'repaired' | 结果：repaired/unrepairable |
| description | VARCHAR | 500 | 是 | NULL | 维修说明 |
| images | TEXT | - | 是 | NULL | 维修凭证图片（JSON数组） |
| teacher_id | INT | - | 否 | - | 登记老师ID |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |

### 2.2.10 报废记录表 (scrap_record)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| device_id | INT | - | 否 | - | 设备ID（外键） |
| scrap_date | DATE | - | 否 | - | 报废日期 |
| reason | ENUM | - | 否 | - | 原因：wear/damage/obsolete/other |
| description | VARCHAR | 500 | 是 | NULL | 详细说明 |
| disposal | ENUM | - | 否 | 'keep' | 处置：keep/discard/recycle |
| teacher_id | INT | - | 否 | - | 登记老师ID |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |

### 2.2.11 公告表 (announcement)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| title | VARCHAR | 100 | 否 | - | 标题 |
| content | TEXT | - | 否 | - | 内容（富文本） |
| attachments | TEXT | - | 是 | NULL | 附件（JSON数组） |
| target_type | VARCHAR | 20 | 否 | 'all' | 范围：all/bio/chem/class/student |
| target_ids | TEXT | - | 是 | NULL | 目标ID列表（JSON数组） |
| is_pinned | TINYINT | - | 否 | 0 | 是否置顶 |
| publish_time | DATETIME | - | 否 | CURRENT_TIMESTAMP | 发布时间 |
| teacher_id | INT | - | 否 | - | 发布老师ID |
| status | TINYINT | - | 否 | 1 | 状态：1正常 0已删除 |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |

### 2.2.12 通知记录表 (notification)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| user_id | INT | - | 否 | - | 接收用户ID |
| user_type | ENUM | - | 否 | 'student' | 用户类型：student/teacher |
| title | VARCHAR | 100 | 否 | - | 通知标题 |
| content | VARCHAR | 500 | 否 | - | 通知内容 |
| type | VARCHAR | 30 | 否 | 'system' | 类型：reservation/borrow/announcement |
| link | VARCHAR | 255 | 是 | NULL | 跳转链接 |
| is_read | TINYINT | - | 否 | 0 | 是否已读 |
| created_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 创建时间 |

### 2.2.13 系统设置表 (system_setting)

| 字段名 | 类型 | 长度 | 允许空 | 默认值 | 说明 |
|--------|------|------|--------|--------|------|
| id | INT | - | 否 | AUTO_INCREMENT | 主键ID |
| setting_key | VARCHAR | 50 | 否 | - | 设置键（唯一） |
| setting_value | TEXT | - | 否 | - | 设置值（JSON格式） |
| description | VARCHAR | 255 | 是 | NULL | 描述 |
| updated_at | DATETIME | - | 否 | CURRENT_TIMESTAMP | 更新时间 |

---

# 三、接口文档

## 3.1 通用规范

### 基础信息

| 项目 | 说明 |
|------|------|
| 基础URL | `/api/v1` |
| 请求方式 | POST / GET / PUT / DELETE |
| 数据格式 | application/json |
| 字符编码 | UTF-8 |

### 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1705315200000
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录/Token无效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 3.2 公共接口

### 3.2.1 用户登录

**接口地址：** `POST /auth/login`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| role | String | 是 | 角色：student/teacher |
| account | String | 是 | 学号/工号 |
| password | String | 是 | 密码 |
| captcha | String | 是 | 验证码 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1,
    "name": "张三",
    "role": "student"
  }
}
```

### 3.2.2 获取验证码

**接口地址：** `GET /auth/captcha`

**响应数据：** 图片验证码（Base64）

### 3.2.3 获取当前用户信息

**接口地址：** `GET /auth/current`

**请求头：** `Authorization: Bearer {token}`

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "张三",
    "account": "20240001",
    "role": "student",
    "labType": "bio"
  }
}
```

### 3.2.4 全局搜索

**接口地址：** `GET /search`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| limit | Integer | 否 | 返回数量，默认10 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "devices": [
      {
        "id": 1,
        "name": "显微镜",
        "code": "DEV-001",
        "category": "bio",
        "status": "available"
      }
    ]
  }
}
```

### 3.2.5 获取未读消息数量

**接口地址：** `GET /notification/unread-count`

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "count": 3
  }
}
```

### 3.2.6 获取消息列表

**接口地址：** `GET /notification/list`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认20 |
| isRead | Integer | 否 | 已读状态：0未读 1已读 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "total": 50,
    "list": [
      {
        "id": 1,
        "title": "预约审核结果",
        "content": "您的显微镜预约已通过审核",
        "type": "reservation",
        "isRead": 0,
        "link": "/reservation/123",
        "createdAt": "2026-01-15 10:30:00"
      }
    ]
  }
}
```

### 3.2.7 标记消息已读

**接口地址：** `PUT /notification/read/{id}`

**路径参数：**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 通知ID |

### 3.2.8 全部标记已读

**接口地址：** `PUT /notification/read-all`

---

## 3.3 学生端接口

### 3.3.1 获取首页数据

**接口地址：** `GET /student/dashboard`

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "student": {
      "id": 1,
      "name": "张三",
      "studentNo": "20240001",
      "labType": "bio",
      "accessExpire": "2026-12-31"
    },
    "stats": {
      "currentBorrowCount": 2,
      "pendingReservationCount": 1
    },
    "announcements": [
      {
        "id": 1,
        "title": "实验室开放时间调整通知",
        "publishTime": "2026-01-10 09:00:00",
        "isRead": false
      }
    ]
  }
}
```

### 3.3.2 获取设备列表

**接口地址：** `GET /student/devices`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryId | Integer | 否 | 分类ID |
| status | String | 否 | 状态：available/borrowed/repair |
| keyword | String | 否 | 搜索关键词 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "name": "显微镜",
        "code": "DEV-001",
        "model": "XSP-200",
        "category": "生物设备",
        "location": "A栋-201-1号柜",
        "status": "available",
        "thumbnail": "/images/device/1.jpg",
        "isFavorited": true
      }
    ]
  }
}
```

### 3.3.3 获取设备详情

**接口地址：** `GET /student/devices/{id}`

**路径参数：**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 设备ID |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "显微镜",
    "code": "DEV-001",
    "category": "生物设备",
    "brand": "奥林巴斯",
    "model": "CX23",
    "spec": "40x-1000x",
    "technicalParams": "LED光源，双目镜筒",
    "location": "A栋-201-1号柜",
    "purchaseDate": "2024-03-15",
    "status": "available",
    "currentBorrower": null,
    "expectedReturnTime": null,
    "images": [
      "/images/device/1_1.jpg",
      "/images/device/1_2.jpg"
    ],
    "availableSlots": [
      {
        "date": "2026-01-16",
        "slots": [
          {"start": "08:00", "end": "10:00", "status": "available"},
          {"start": "10:00", "end": "12:00", "status": "available"}
        ]
      }
    ],
    "comments": [
      {
        "userName": "李四",
        "rating": 5,
        "content": "设备很好用",
        "createTime": "2026-01-10 14:30:00",
        "likeCount": 3
      }
    ]
  }
}
```

### 3.3.4 收藏/取消收藏设备

**接口地址：** `POST /student/favorites`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceId | Long | 是 | 设备ID |
| action | String | 是 | add/remove |

### 3.3.5 获取收藏列表

**接口地址：** `GET /student/favorites`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

### 3.3.6 提交预约申请

**接口地址：** `POST /student/reservations`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceId | Long | 是 | 设备ID |
| startTime | String | 是 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | String | 是 | 结束时间 |
| purpose | String | 是 | 用途说明 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "reservationId": 123
  }
}
```

### 3.3.7 获取我的预约列表

**接口地址：** `GET /student/reservations`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 否 | current/history，默认current |
| status | String | 否 | pending/approved/rejected/cancelled |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "total": 10,
    "list": [
      {
        "id": 123,
        "deviceName": "显微镜",
        "deviceCode": "DEV-001",
        "startTime": "2026-01-20 08:00:00",
        "endTime": "2026-01-20 12:00:00",
        "purpose": "细胞观察实验",
        "status": "pending",
        "reason": null,
        "createdAt": "2026-01-15 10:30:00"
      }
    ]
  }
}
```

### 3.3.8 取消预约

**接口地址：** `PUT /student/reservations/{id}/cancel`

### 3.3.9 申请延期

**接口地址：** `POST /student/reservations/{id}/extend`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| newEndTime | String | 是 | 新的归还时间 |
| reason | String | 是 | 延期理由 |

### 3.3.10 获取我的借用记录

**接口地址：** `GET /student/borrows`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 否 | current/history，默认current |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "total": 5,
    "list": [
      {
        "id": 1,
        "deviceName": "显微镜",
        "deviceCode": "DEV-001",
        "borrowTime": "2026-01-10 09:00:00",
        "dueTime": "2026-01-13 09:00:00",
        "remainingDays": 2,
        "isOverdue": false,
        "returnCode": "RET-001-123",
        "equipmentCondition": null,
        "violation": null
      }
    ]
  }
}
```

### 3.3.11 提交违规申诉

**接口地址：** `POST /student/violations/{id}/appeal`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 是 | 申诉理由 |

### 3.3.12 获取个人中心数据

**接口地址：** `GET /student/profile`

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "student": {
      "name": "张三",
      "studentNo": "20240001",
      "class": "生物技术1班",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "accessStatus": "normal",
      "accessExpire": "2026-12-31"
    },
    "violations": [
      {
        "id": 1,
        "time": "2025-12-20",
        "deviceName": "离心机",
        "type": "overdue",
        "punishment": "warning",
        "teacherName": "李老师"
      }
    ]
  }
}
```

### 3.3.13 更新个人资料

**接口地址：** `PUT /student/profile`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 否 | 联系电话 |
| email | String | 否 | 邮箱 |

---

## 3.4 老师端接口

### 3.4.1 获取首页数据

**接口地址：** `GET /teacher/dashboard`

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "stats": {
      "deviceCount": 156,
      "availableCount": 120,
      "repairCount": 5,
      "todayReservationCount": 8,
      "pendingAuditCount": 3,
      "borrowedCount": 25,
      "overdueCount": 2,
      "activeStudentCount": 45,
      "violationStudentCount": 8
    },
    "todos": [
      {
        "id": 1,
        "type": "overdue",
        "deviceName": "显微镜",
        "studentName": "张三",
        "time": "2天",
        "priority": "high"
      }
    ],
    "charts": {
      "topDevices": [
        {"name": "显微镜", "count": 45},
        {"name": "离心机", "count": 32}
      ],
      "monthlyTrend": [
        {"month": "2025-12", "count": 120},
        {"month": "2026-01", "count": 85}
      ]
    }
  }
}
```

### 3.4.2 获取设备列表（老师端）

**接口地址：** `GET /teacher/devices`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryId | Integer | 否 | 分类ID |
| status | String | 否 | 状态 |
| keyword | String | 否 | 关键词 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "total": 156,
    "list": [
      {
        "id": 1,
        "name": "显微镜",
        "code": "DEV-001",
        "category": "生物设备",
        "model": "CX23",
        "location": "A-201",
        "purchaseDate": "2024-03-15",
        "warrantyDate": "2026-03-15",
        "status": "available"
      }
    ]
  }
}
```

### 3.4.3 添加/编辑设备

**接口地址：** `POST /teacher/devices` (新增) / `PUT /teacher/devices/{id}` (编辑)

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 设备名称 |
| code | String | 是 | 设备编号 |
| categoryId | Integer | 是 | 分类ID |
| brand | String | 否 | 品牌 |
| model | String | 否 | 型号 |
| spec | String | 否 | 规格参数 |
| technicalParams | String | 否 | 技术参数 |
| location | String | 是 | 存放位置 |
| purchaseDate | String | 否 | 购入日期 |
| warrantyDate | String | 否 | 保修截止日期 |
| description | String | 否 | 使用说明 |
| images | Array | 否 | 图片URL数组 |

### 3.4.4 删除设备

**接口地址：** `DELETE /teacher/devices/{id}`

### 3.4.5 修改设备状态

**接口地址：** `PUT /teacher/devices/{id}/status`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | repair/scrap |
| reason | String | 条件必填 | 维修/报废原因 |

### 3.4.6 生成设备二维码

**接口地址：** `POST /teacher/devices/qr-codes`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceIds | Array | 是 | 设备ID数组 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "pdfUrl": "/download/qr-codes-xxx.pdf"
  }
}
```

### 3.4.7 获取预约列表

**接口地址：** `GET /teacher/reservations`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 否 | pending/approved/rejected |
| studentName | String | 否 | 学生姓名 |
| deviceName | String | 否 | 设备名称 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "total": 10,
    "list": [
      {
        "id": 1,
        "studentName": "张三",
        "studentNo": "20240001",
        "deviceName": "显微镜",
        "deviceCode": "DEV-001",
        "startTime": "2026-01-20 08:00:00",
        "endTime": "2026-01-20 12:00:00",
        "purpose": "细胞观察实验",
        "status": "pending",
        "waitingHours": 12,
        "createdAt": "2026-01-15 10:30:00"
      }
    ]
  }
}
```

### 3.4.8 审核预约

**接口地址：** `PUT /teacher/reservations/{id}/audit`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| result | String | 是 | approve/reject |
| reason | String | 条件必填 | 拒绝时必填 |

### 3.4.9 批量审核预约

**接口地址：** `POST /teacher/reservations/batch-audit`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | Array | 是 | 预约ID数组 |
| result | String | 是 | approve/reject |
| reason | String | 否 | 统一拒绝理由 |

### 3.4.10 借用登记

**接口地址：** `POST /teacher/borrows`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceCode | String | 是 | 设备编号（扫码） |
| studentNo | String | 是 | 学生学号 |
| dueTime | String | 是 | 应还时间 |
| remark | String | 否 | 备注 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "borrowId": 1,
    "returnCode": "RET-001-123"
  }
}
```

### 3.4.11 归还登记

**接口地址：** `POST /teacher/borrows/return`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceCode | String | 是 | 设备编号（扫码） |
| equipmentCondition | String | 是 | good/worn/damaged/clean |
| damagedImages | Array | 条件必填 | 损坏时上传图片 |
| violationType | String | 否 | 违规类型：overdue/damage/none |
| violationDescription | String | 条件必填 | 违规说明 |

### 3.4.12 获取当前借用列表

**接口地址：** `GET /teacher/borrows/current`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 设备名/学生名 |
| isOverdue | Boolean | 否 | 是否超时 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "deviceName": "显微镜",
        "deviceCode": "DEV-001",
        "studentName": "张三",
        "studentNo": "20240001",
        "borrowTime": "2026-01-10 09:00:00",
        "dueTime": "2026-01-13 09:00:00",
        "overdueDays": 2,
        "status": "overdue"
      }
    ]
  }
}
```

### 3.4.13 催还通知

**接口地址：** `POST /teacher/borrows/{id}/remind`

### 3.4.14 获取学生列表

**接口地址：** `GET /teacher/students`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| class | String | 否 | 班级 |
| accessStatus | Integer | 否 | 准入状态 |
| keyword | String | 否 | 姓名/学号 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "total": 200,
    "list": [
      {
        "id": 1,
        "name": "张三",
        "studentNo": "20240001",
        "class": "生物技术1班",
        "phone": "13800138000",
        "accessStatus": "normal",
        "currentBorrowCount": 2,
        "totalBorrowCount": 15,
        "violationCount": 1
      }
    ]
  }
}
```

### 3.4.15 禁用/启用学生权限

**接口地址：** `PUT /teacher/students/{id}/access`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 是 | 1正常 2禁用 |
| reason | String | 条件必填 | 禁用原因 |
| banDays | Integer | 否 | 临时禁用天数 |

### 3.4.16 添加违规记录

**接口地址：** `POST /teacher/violations`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| studentId | Long | 是 | 学生ID |
| borrowId | Long | 否 | 借用记录ID |
| type | String | 是 | overdue/damage/other |
| violationTime | String | 是 | 违规时间 |
| punishment | String | 是 | warning/ban/compensation |
| banDays | Integer | 条件必填 | 禁借用天数 |
| compensationAmount | BigDecimal | 条件必填 | 赔偿金额 |
| description | String | 是 | 违规说明 |

### 3.4.17 获取维修列表

**接口地址：** `GET /teacher/repairs`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 否 | pending/repaired |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

### 3.4.18 登记维修

**接口地址：** `POST /teacher/repairs`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceId | Long | 是 | 设备ID |
| repairDate | String | 是 | 维修日期 |
| repairPerson | String | 是 | 维修人员 |
| cost | BigDecimal | 否 | 维修费用 |
| result | String | 是 | repaired/unrepairable |
| description | String | 否 | 说明 |
| images | Array | 否 | 凭证图片 |

### 3.4.19 发布公告

**接口地址：** `POST /teacher/announcements`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 是 | 标题 |
| content | String | 是 | 内容 |
| attachments | Array | 否 | 附件 |
| targetType | String | 是 | all/bio/chem/class/student |
| targetIds | Array | 条件必填 | 目标ID列表 |
| isPinned | Boolean | 否 | 是否置顶 |
| publishTime | String | 否 | 定时发布时间 |

### 3.4.20 获取公告列表

**接口地址：** `GET /teacher/announcements`

### 3.4.21 删除公告

**接口地址：** `DELETE /teacher/announcements/{id}`

### 3.4.22 获取统计数据

**接口地址：** `GET /teacher/statistics`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "deviceStats": {
      "rankings": [{"name": "显微镜", "count": 45}],
      "categoryRatio": {"bio": 0.6, "chem": 0.4},
      "monthlyTrend": [{"month": "2026-01", "count": 85}]
    },
    "studentStats": {
      "topStudents": [{"name": "张三", "count": 25}],
      "classActivity": [{"class": "生物技术1班", "avgCount": 3.2}]
    },
    "violationStats": {
      "typeRatio": {"overdue": 0.7, "damage": 0.2, "other": 0.1},
      "monthlyTrend": [{"month": "2026-01", "count": 5}]
    }
  }
}
```

### 3.4.23 生成报表

**接口地址：** `POST /teacher/reports/generate`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reportType | String | 是 | monthly/semester/yearly |
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |
| includeModules | Array | 是 | 包含模块 |
| format | String | 是 | excel/pdf |

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "downloadUrl": "/download/report-xxx.pdf"
  }
}
```

### 3.4.24 获取系统设置

**接口地址：** `GET /teacher/settings`

**响应数据：**

```json
{
  "code": 200,
  "data": {
    "labInfo": {
      "name": "生物化学实验室",
      "openHours": [{"days": "1-5", "time": "08:00-17:00"}],
      "rules": "请遵守实验室规定..."
    },
    "reservationRules": {
      "maxDuration": 3,
      "maxAdvanceDays": 7,
      "cancelAdvanceHours": 24,
      "maxBorrowCount": 3,
      "slotGranularity": 2
    },
    "reminderSettings": {
      "overdueThreshold": 24,
      "remindMethods": ["sms", "inapp"],
      "remindInterval": "daily",
      "advanceRemindHours": 2
    }
  }
}
```

### 3.4.25 更新系统设置

**接口地址：** `PUT /teacher/settings`

**请求参数：** 同上结构

---

以上为完整的系统设计文档，包含页面内容设计、数据库表结构（13张表）和接口文档（约40个接口）。