-- ============================================
-- 实验室设备仪器管理系统 - 数据库初始化脚本
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 学生表 (student)
-- ============================================
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_no` VARCHAR(20) NOT NULL COMMENT '学号（唯一）',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `class_name` VARCHAR(50) NOT NULL COMMENT '班级',
  `phone` VARCHAR(11) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `lab_type` ENUM('bio','chem') NOT NULL DEFAULT 'bio' COMMENT '实验室类型：bio/chem',
  `access_status` TINYINT NOT NULL DEFAULT 1 COMMENT '准入状态：1正常 2禁用',
  `access_expire` DATE DEFAULT NULL COMMENT '准入有效期',
  `violation_count` INT NOT NULL DEFAULT 0 COMMENT '违规次数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账户状态：1正常 0禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- ============================================
-- 2. 老师表 (teacher)
-- ============================================
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `teacher_no` VARCHAR(20) NOT NULL COMMENT '工号（唯一）',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(11) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `role` VARCHAR(20) NOT NULL DEFAULT 'teacher' COMMENT '角色：teacher/admin',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账户状态：1正常 0禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_no` (`teacher_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老师表';

-- ============================================
-- 3. 设备分类表 (device_category)
-- ============================================
DROP TABLE IF EXISTS `device_category`;
CREATE TABLE `device_category` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` INT DEFAULT 0 COMMENT '父级ID（0为顶级）',
  `lab_type` ENUM('bio','chem') DEFAULT NULL COMMENT '所属实验室：bio/chem',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备分类表';

-- ============================================
-- 4. 设备表 (device)
-- ============================================
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(50) NOT NULL COMMENT '设备编号（唯一）',
  `name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `category_id` INT NOT NULL COMMENT '分类ID（外键）',
  `brand` VARCHAR(50) DEFAULT NULL COMMENT '品牌',
  `model` VARCHAR(100) DEFAULT NULL COMMENT '型号',
  `spec` TEXT COMMENT '规格参数',
  `technical_params` TEXT COMMENT '技术参数',
  `location` VARCHAR(100) NOT NULL COMMENT '存放位置',
  `purchase_date` DATE DEFAULT NULL COMMENT '购入日期',
  `warranty_date` DATE DEFAULT NULL COMMENT '保修截止日期',
  `status` ENUM('available','borrowed','repair','scrap') NOT NULL DEFAULT 'available' COMMENT '状态：available/borrowed/repair/scrap',
  `current_borrower_id` INT DEFAULT NULL COMMENT '当前借用人ID',
  `expected_return_time` DATETIME DEFAULT NULL COMMENT '预计归还时间',
  `description` TEXT COMMENT '使用说明',
  `qr_code` VARCHAR(255) DEFAULT NULL COMMENT '二维码标识',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_code` (`code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- ============================================
-- 5. 设备图片表 (device_image)
-- ============================================
DROP TABLE IF EXISTS `device_image`;
CREATE TABLE `device_image` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` INT NOT NULL COMMENT '设备ID（外键）',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备图片表';

-- ============================================
-- 6. 预约表 (reservation)
-- ============================================
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` INT NOT NULL COMMENT '学生ID（外键）',
  `device_id` INT NOT NULL COMMENT '设备ID（外键）',
  `start_time` DATETIME NOT NULL COMMENT '预约开始时间',
  `end_time` DATETIME NOT NULL COMMENT '预约结束时间',
  `purpose` VARCHAR(255) NOT NULL COMMENT '用途说明',
  `status` ENUM('pending','approved','rejected','cancelled','extending') NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected/cancelled/extending',
  `reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝/取消原因',
  `teacher_id` INT DEFAULT NULL COMMENT '审核老师ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ============================================
-- 7. 借用记录表 (borrow_record)
-- ============================================
DROP TABLE IF EXISTS `borrow_record`;
CREATE TABLE `borrow_record` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` INT NOT NULL COMMENT '学生ID（外键）',
  `device_id` INT NOT NULL COMMENT '设备ID（外键）',
  `teacher_id` INT DEFAULT NULL COMMENT '登记老师ID',
  `borrow_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借用时间',
  `due_time` DATETIME NOT NULL COMMENT '应还时间',
  `return_time` DATETIME DEFAULT NULL COMMENT '实际归还时间',
  `status` ENUM('borrowed','returned','overdue') NOT NULL DEFAULT 'borrowed' COMMENT '状态：borrowed/returned/overdue',
  `equipment_condition` ENUM('good','worn','damaged','clean') DEFAULT NULL COMMENT '归还时状态：good/worn/damaged/clean',
  `is_overdue` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超时：0否 1是',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借用记录表';

-- ============================================
-- 8. 违规记录表 (violation)
-- ============================================
DROP TABLE IF EXISTS `violation`;
CREATE TABLE `violation` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` INT NOT NULL COMMENT '学生ID（外键）',
  `borrow_id` INT DEFAULT NULL COMMENT '关联借用记录ID',
  `type` ENUM('overdue','damage','other') NOT NULL COMMENT '类型：overdue/damage/other',
  `violation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '违规时间',
  `punishment` ENUM('warning','ban','compensation') NOT NULL DEFAULT 'warning' COMMENT '处罚：warning/ban/compensation',
  `ban_days` INT DEFAULT NULL COMMENT '禁借用天数',
  `compensation_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '赔偿金额',
  `description` VARCHAR(500) NOT NULL COMMENT '违规说明',
  `teacher_id` INT NOT NULL COMMENT '处理老师ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1有效 0已撤销',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_borrow_id` (`borrow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规记录表';

-- ============================================
-- 9. 维修记录表 (repair_record)
-- ============================================
DROP TABLE IF EXISTS `repair_record`;
CREATE TABLE `repair_record` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` INT NOT NULL COMMENT '设备ID（外键）',
  `repair_date` DATE NOT NULL COMMENT '维修日期',
  `repair_person` VARCHAR(50) NOT NULL COMMENT '维修人员',
  `cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
  `result` ENUM('repaired','unrepairable') NOT NULL DEFAULT 'repaired' COMMENT '结果：repaired/unrepairable',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '维修说明',
  `images` TEXT COMMENT '维修凭证图片（JSON数组）',
  `teacher_id` INT NOT NULL COMMENT '登记老师ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修记录表';

-- ============================================
-- 10. 报废记录表 (scrap_record)
-- ============================================
DROP TABLE IF EXISTS `scrap_record`;
CREATE TABLE `scrap_record` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` INT DEFAULT NULL COMMENT '设备ID（外键，可为空表示设备已删除）',
  `scrap_date` DATE NOT NULL COMMENT '报废日期',
  `reason` ENUM('wear','damage','obsolete','other') NOT NULL COMMENT '原因：wear/damage/obsolete/other',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '详细说明',
  `disposal` ENUM('keep','discard','recycle') NOT NULL DEFAULT 'keep' COMMENT '处置：keep/discard/recycle',
  `teacher_id` INT NOT NULL COMMENT '登记老师ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报废记录表';

-- ============================================
-- 11. 公告表 (announcement)
-- ============================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容（富文本）',
  `attachments` TEXT COMMENT '附件（JSON数组）',
  `target_type` VARCHAR(20) NOT NULL DEFAULT 'all' COMMENT '范围：all/bio/chem/class/student',
  `target_ids` TEXT COMMENT '目标ID列表（JSON数组）',
  `is_pinned` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `publish_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `teacher_id` INT NOT NULL COMMENT '发布老师ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0已删除',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ============================================
-- 12. 通知记录表 (notification)
-- ============================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT NOT NULL COMMENT '接收用户ID',
  `user_type` ENUM('student','teacher') NOT NULL DEFAULT 'student' COMMENT '用户类型：student/teacher',
  `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
  `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
  `type` VARCHAR(30) NOT NULL DEFAULT 'system' COMMENT '类型：reservation/borrow/announcement',
  `link` VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';

-- ============================================
-- 13. 系统设置表 (system_setting)
-- ============================================
DROP TABLE IF EXISTS `system_setting`;
CREATE TABLE `system_setting` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `setting_key` VARCHAR(50) NOT NULL COMMENT '设置键（唯一）',
  `setting_value` TEXT NOT NULL COMMENT '设置值（JSON格式）',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

SET FOREIGN_KEY_CHECKS = 1;
