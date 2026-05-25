-- ============================================
-- 公告已读记录表 (announcement_read)
-- 增量更新脚本 - 仅添加新表，不影响现有数据
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;

-- 创建公告已读记录表
CREATE TABLE IF NOT EXISTS `announcement_read` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` INT NOT NULL COMMENT '学生ID（外键）',
  `announcement_id` INT NOT NULL COMMENT '公告ID（外键）',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_announcement` (`student_id`, `announcement_id`),
  KEY `idx_announcement_id` (`announcement_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录表';
