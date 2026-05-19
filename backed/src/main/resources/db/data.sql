-- ============================================
-- 实验室设备仪器管理系统 - 模拟数据
-- ============================================

SET NAMES utf8mb4;

-- ============================================
-- 1. 插入老师数据
-- ============================================
INSERT INTO `teacher` (`teacher_no`, `name`, `phone`, `email`, `password`, `role`, `status`) VALUES
('T001', '李老师', '13800138001', 'li@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin', 1),
('T002', '王老师', '13800138002', 'wang@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'teacher', 1),
('T003', '张老师', '13800138003', 'zhang@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'teacher', 1);

-- ============================================
-- 2. 插入学生数据
-- ============================================
INSERT INTO `student` (`student_no`, `name`, `class_name`, `phone`, `email`, `password`, `lab_type`, `access_status`, `access_expire`, `violation_count`, `status`) VALUES
('2024001', '张三', '生物技术1班', '13900139001', 'zhangsan@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'bio', 1, '2026-12-31', 0, 1),
('2024002', '李四', '生物技术1班', '13900139002', 'lisi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'bio', 1, '2026-12-31', 1, 1),
('2024003', '王五', '生物技术2班', '13900139003', 'wangwu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'bio', 1, '2026-12-31', 0, 1),
('2024004', '赵六', '化学工程1班', '13900139004', 'zhaoliu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'chem', 1, '2026-12-31', 0, 1),
('2024005', '孙七', '化学工程1班', '13900139005', 'sunqi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'chem', 1, '2026-12-31', 2, 1),
('2024006', '周八', '化学工程2班', '13900139006', 'zhouba@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'chem', 1, '2026-12-31', 0, 1),
('2024007', '吴九', '生物技术1班', '13900139007', 'wujiu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'bio', 2, '2026-06-30', 3, 1),
('2024008', '郑十', '生物技术2班', '13900139008', 'zhengshi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'bio', 1, '2026-12-31', 0, 1),
('2024009', '陈一', '化学工程1班', '13900139009', 'chenyi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'chem', 1, '2026-12-31', 0, 1),
('2024010', '林二', '化学工程2班', '13900139010', 'liner@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'chem', 1, '2026-12-31', 0, 1);

-- ============================================
-- 3. 插入设备分类数据
-- ============================================
INSERT INTO `device_category` (`name`, `parent_id`, `lab_type`, `sort_order`, `status`) VALUES
('生物设备', 0, 'bio', 1, 1),
('化学设备', 0, 'chem', 2, 1),
('显微镜', 1, 'bio', 1, 1),
('离心机', 1, 'bio', 2, 1),
('培养箱', 1, 'bio', 3, 1),
('分光光度计', 1, 'bio', 4, 1),
('反应釜', 2, 'chem', 1, 1),
('滴定仪', 2, 'chem', 2, 1),
('色谱仪', 2, 'chem', 3, 1),
('pH计', 2, 'chem', 4, 1);

-- ============================================
-- 4. 插入设备数据
-- ============================================
INSERT INTO `device` (`code`, `name`, `category_id`, `brand`, `model`, `spec`, `technical_params`, `location`, `purchase_date`, `warranty_date`, `status`, `description`, `qr_code`) VALUES
('DEV-BIO-001', '光学显微镜', 3, '奥林巴斯', 'CX23', '40x-1000x', 'LED光源，双目镜筒，无限远光学系统', 'A栋-201-1号柜', '2024-03-15', '2026-03-15', 'available', '用于细胞观察和微生物研究', 'QR-DEV-BIO-001'),
('DEV-BIO-002', '电子显微镜', 3, '日立', 'SU3500', '放大倍数：200000x', '扫描电镜，配备能谱仪', 'A栋-202-1号柜', '2023-06-20', '2026-06-20', 'available', '用于微观结构分析', 'QR-DEV-BIO-002'),
('DEV-BIO-003', '高速离心机', 4, 'Eppendorf', '5430R', '最高转速：30000rpm', '制冷型，最大容量：6×50ml', 'A栋-201-2号柜', '2024-01-10', '2026-01-10', 'borrowed', '用于样品分离和纯化', 'QR-DEV-BIO-003'),
('DEV-BIO-004', '低速离心机', 4, '湘仪', 'TD5A', '最高转速：5000rpm', '常温型，最大容量：6×500ml', 'A栋-201-3号柜', '2023-09-05', '2025-09-05', 'available', '用于常规样品离心', 'QR-DEV-BIO-004'),
('DEV-BIO-005', 'CO2培养箱', 5, 'Thermo', '3111', '温度范围：室温+5~60℃', 'CO2浓度控制：0-20%，湿度控制', 'A栋-203-1号柜', '2024-05-20', '2026-05-20', 'available', '用于细胞培养', 'QR-DEV-BIO-005'),
('DEV-BIO-006', '恒温培养箱', 5, '上海一恒', 'DHP-9162', '温度范围：室温+5~65℃', '容积：160L，微电脑控温', 'A栋-203-2号柜', '2023-11-15', '2025-11-15', 'repair', '用于微生物培养', 'QR-DEV-BIO-006'),
('DEV-BIO-007', '紫外可见分光光度计', 6, '岛津', 'UV-2600', '波长范围：190-1100nm', '双光束，自动波长扫描', 'A栋-204-1号柜', '2024-02-28', '2026-02-28', 'available', '用于物质浓度测定', 'QR-DEV-BIO-007'),
('DEV-CHEM-001', '高压反应釜', 7, ' Parr', '4560', '容积：1L，压力：20MPa', '不锈钢材质，带搅拌装置', 'B栋-301-1号柜', '2023-08-10', '2026-08-10', 'available', '用于高温高压反应', 'QR-DEV-CHEM-001'),
('DEV-CHEM-002', '微型反应釜', 7, '天津欧诺', 'KCF-0.1', '容积：0.1L，压力：10MPa', '小型实验用，聚四氟乙烯内衬', 'B栋-301-2号柜', '2024-04-15', '2026-04-15', 'available', '用于小批量反应实验', 'QR-DEV-CHEM-002'),
('DEV-CHEM-003', '自动电位滴定仪', 8, '梅特勒', 'T50', '精度：0.001ml', '自动终点判断，多通道', 'B栋-302-1号柜', '2024-06-01', '2026-06-01', 'borrowed', '用于酸碱滴定分析', 'QR-DEV-CHEM-003'),
('DEV-CHEM-004', '手动滴定仪', 8, '上海雷磁', 'ZDJ-4A', '精度：0.01ml', '数字显示，手动控制', 'B栋-302-2号柜', '2023-10-20', '2025-10-20', 'available', '用于基础滴定实验', 'QR-DEV-CHEM-004'),
('DEV-CHEM-005', '气相色谱仪', 9, '安捷伦', '7890B', '检测器：FID/TCD/ECD', '毛细管柱，自动进样', 'B栋-303-1号柜', '2023-07-15', '2026-07-15', 'available', '用于有机物分离分析', 'QR-DEV-CHEM-005'),
('DEV-CHEM-006', '液相色谱仪', 9, '沃特世', 'e2695', '检测器：UV/FLD', '二元梯度泵，自动进样器', 'B栋-303-2号柜', '2024-03-10', '2026-03-10', 'available', '用于大分子化合物分析', 'QR-DEV-CHEM-006'),
('DEV-CHEM-007', '精密pH计', 10, '梅特勒', 'FE28', '精度：±0.01pH', '自动温度补偿，校准提醒', 'B栋-304-1号柜', '2024-01-25', '2026-01-25', 'available', '用于溶液pH值测量', 'QR-DEV-CHEM-007'),
('DEV-CHEM-008', '便携式pH计', 10, '哈希', 'HQ11d', '精度：±0.1pH', '电池供电，防水设计', 'B栋-304-2号柜', '2023-12-05', '2025-12-05', 'available', '用于现场pH测量', 'QR-DEV-CHEM-008');

-- ============================================
-- 5. 插入设备图片数据
-- ============================================
INSERT INTO `device_image` (`device_id`, `image_url`, `sort_order`) VALUES
(1, '/images/devices/microscope_1.jpg', 1),
(1, '/images/devices/microscope_2.jpg', 2),
(2, '/images/devices/emicroscope_1.jpg', 1),
(3, '/images/devices/centrifuge_high_1.jpg', 1),
(3, '/images/devices/centrifuge_high_2.jpg', 2),
(4, '/images/devices/centrifuge_low_1.jpg', 1),
(5, '/images/devices/incubator_co2_1.jpg', 1),
(6, '/images/devices/incubator_const_1.jpg', 1),
(7, '/images/devices/spectrophotometer_1.jpg', 1),
(8, '/images/devices/reactor_high_1.jpg', 1),
(9, '/images/devices/reactor_micro_1.jpg', 1),
(10, '/images/devices/titrator_auto_1.jpg', 1),
(11, '/images/devices/titrator_manual_1.jpg', 1),
(12, '/images/devices/gc_1.jpg', 1),
(13, '/images/devices/hplc_1.jpg', 1),
(14, '/images/devices/ph_meter_precision_1.jpg', 1),
(15, '/images/devices/ph_meter_portable_1.jpg', 1);

-- ============================================
-- 6. 插入预约数据
-- ============================================
INSERT INTO `reservation` (`student_id`, `device_id`, `start_time`, `end_time`, `purpose`, `status`, `reason`, `teacher_id`, `audit_time`, `created_at`) VALUES
(1, 1, '2026-01-20 08:00:00', '2026-01-20 12:00:00', '细胞观察实验', 'approved', NULL, 1, '2026-01-15 14:30:00', '2026-01-15 10:30:00'),
(1, 3, '2026-01-22 14:00:00', '2026-01-22 18:00:00', '蛋白质分离实验', 'pending', NULL, NULL, NULL, '2026-01-16 09:00:00'),
(2, 7, '2026-01-21 09:00:00', '2026-01-21 11:00:00', '酶活性测定', 'approved', NULL, 2, '2026-01-16 10:00:00', '2026-01-15 16:00:00'),
(3, 5, '2026-01-23 08:00:00', '2026-01-23 17:00:00', '细胞培养实验', 'rejected', '设备维护中', 1, '2026-01-16 11:00:00', '2026-01-16 08:00:00'),
(4, 8, '2026-01-24 10:00:00', '2026-01-24 16:00:00', '有机合成反应', 'pending', NULL, NULL, NULL, '2026-01-16 14:00:00'),
(5, 10, '2026-01-25 09:00:00', '2026-01-25 12:00:00', '酸碱滴定分析', 'approved', NULL, 3, '2026-01-16 15:00:00', '2026-01-16 10:00:00'),
(6, 12, '2026-01-26 14:00:00', '2026-01-26 17:00:00', '挥发性有机物检测', 'cancelled', '实验计划变更', NULL, NULL, '2026-01-15 11:00:00'),
(8, 1, '2026-01-27 08:00:00', '2026-01-27 12:00:00', '微生物形态观察', 'pending', NULL, NULL, NULL, '2026-01-17 09:00:00');

-- ============================================
-- 7. 插入借用记录数据
-- ============================================
INSERT INTO `borrow_record` (`student_id`, `device_id`, `teacher_id`, `borrow_time`, `due_time`, `return_time`, `status`, `equipment_condition`, `is_overdue`, `remark`) VALUES
(1, 3, 1, '2026-01-10 09:00:00', '2026-01-13 09:00:00', '2026-01-12 16:00:00', 'returned', 'good', 0, '按时归还'),
(2, 10, 2, '2026-01-08 10:00:00', '2026-01-11 10:00:00', '2026-01-13 14:00:00', 'returned', 'worn', 1, '超时2天归还'),
(4, 8, 1, '2026-01-15 08:00:00', '2026-01-18 08:00:00', NULL, 'borrowed', NULL, 0, NULL),
(5, 12, 3, '2026-01-12 09:00:00', '2026-01-15 09:00:00', NULL, 'overdue', NULL, 1, '已超时'),
(3, 1, 2, '2026-01-14 14:00:00', '2026-01-17 14:00:00', '2026-01-17 10:00:00', 'returned', 'good', 0, '提前归还'),
(6, 14, 1, '2026-01-16 10:00:00', '2026-01-19 10:00:00', NULL, 'borrowed', NULL, 0, NULL),
(1, 7, 2, '2026-01-11 09:00:00', '2026-01-14 09:00:00', '2026-01-14 08:30:00', 'returned', 'good', 0, NULL),
(8, 5, 3, '2026-01-13 08:00:00', '2026-01-16 08:00:00', '2026-01-18 15:00:00', 'returned', 'clean', 1, '超时2天，已清洁');

-- ============================================
-- 8. 插入违规记录数据
-- ============================================
INSERT INTO `violation` (`student_id`, `borrow_id`, `type`, `violation_time`, `punishment`, `ban_days`, `compensation_amount`, `description`, `teacher_id`, `status`) VALUES
(2, 2, 'overdue', '2026-01-13 14:00:00', 'warning', NULL, NULL, '超时归还设备', 2, 1),
(5, 4, 'overdue', '2026-01-15 09:00:00', 'ban', 7, NULL, '超时未还，暂停借用权限7天', 3, 1),
(5, NULL, 'damage', '2025-12-20 10:00:00', 'compensation', NULL, 200.00, '损坏pH计电极，需赔偿', 1, 1),
(8, 8, 'overdue', '2026-01-16 08:00:00', 'warning', NULL, NULL, '超时归还培养箱', 3, 1),
(7, NULL, 'other', '2025-11-15 14:00:00', 'ban', 30, NULL, '违反实验室安全规定，禁用30天', 1, 1);

-- ============================================
-- 9. 插入维修记录数据
-- ============================================
INSERT INTO `repair_record` (`device_id`, `repair_date`, `repair_person`, `cost`, `result`, `description`, `images`, `teacher_id`) VALUES
(6, '2026-01-15', '刘工程师', 1500.00, 'repaired', '温控系统故障，更换温控模块', '["/images/repairs/repair_6_1.jpg"]', 1),
(3, '2026-01-10', '张技师', 800.00, 'repaired', '转子不平衡，重新校准', '["/images/repairs/repair_3_1.jpg", "/images/repairs/repair_3_2.jpg"]', 2),
(11, '2025-12-28', '李工程师', 3500.00, 'repaired', '泵头密封件老化，更换密封组件', NULL, 1);

-- ============================================
-- 10. 插入报废记录数据
-- ============================================
INSERT INTO `scrap_record` (`device_id`, `scrap_date`, `reason`, `description`, `disposal`, `teacher_id`) VALUES
(NULL, '2025-11-20', 'wear', '老旧pH计，使用年限超过10年，精度不达标', 'discard', 1);

-- ============================================
-- 11. 插入公告数据
-- ============================================
INSERT INTO `announcement` (`title`, `content`, `attachments`, `target_type`, `target_ids`, `is_pinned`, `publish_time`, `teacher_id`, `status`) VALUES
('实验室开放时间调整通知', '<p>各位同学：</p><p>自2026年1月20日起，实验室开放时间调整为：</p><ul><li>周一至周五：8:00-21:00</li><li>周六：9:00-17:00</li><li>周日：闭馆</li></ul><p>请大家合理安排实验时间。</p>', NULL, 'all', NULL, 1, '2026-01-10 09:00:00', 1, 1),
('新设备投入使用通知', '<p>本实验室新购入以下设备：</p><ul><li>高效液相色谱仪（HPLC）</li><li>实时荧光定量PCR仪</li></ul><p>即日起开放预约使用，使用前请参加操作培训。</p>', '["/attachments/new_device_manual.pdf"]', 'all', NULL, 0, '2026-01-12 10:00:00', 2, 1),
('春节放假安排', '<p>根据学校安排，实验室春节放假时间为：</p><p>2026年2月15日 - 2月23日</p><p>期间暂停所有设备借用和预约服务。</p>', NULL, 'all', NULL, 0, '2026-01-15 14:00:00', 1, 1),
('生物实验室安全培训通知', '<p>所有生物实验室准入人员必须参加安全培训：</p><p>时间：2026年1月25日 14:00-16:00</p><p>地点：A栋201会议室</p>', NULL, 'bio', NULL, 0, '2026-01-16 09:00:00', 2, 1),
('化学试剂管理规范更新', '<p>新版化学试剂管理规定已发布，主要变更：</p><ol><li>剧毒试剂实行双人双锁管理</li><li>易燃试剂存放量不得超过一周用量</li><li>废弃试剂必须分类收集</li></ol>', '["/attachments/chemical_regulation_2026.pdf"]', 'chem', NULL, 0, '2026-01-17 11:00:00', 3, 1);

-- ============================================
-- 12. 插入通知数据
-- ============================================
INSERT INTO `notification` (`user_id`, `user_type`, `title`, `content`, `type`, `link`, `is_read`, `created_at`) VALUES
(1, 'student', '预约审核结果', '您的显微镜预约已通过审核', 'reservation', '/student/reservations', 0, '2026-01-15 14:30:00'),
(1, 'student', '借用到期提醒', '您借用的离心机将于明天到期，请及时归还', 'borrow', '/student/borrows', 0, '2026-01-12 09:00:00'),
(2, 'student', '预约审核结果', '您的分光光度计预约已通过审核', 'reservation', '/student/reservations', 1, '2026-01-16 10:00:00'),
(2, 'student', '违规警告', '您因超时归还有1次违规记录', 'system', '/student/profile', 1, '2026-01-13 14:00:00'),
(3, 'student', '预约被拒绝', '您的CO2培养箱预约被拒绝：设备维护中', 'reservation', '/student/reservations', 0, '2026-01-16 11:00:00'),
(4, 'student', '预约提交成功', '您的高压反应釜预约已提交，等待审核', 'reservation', '/student/reservations', 0, '2026-01-16 14:00:00'),
(5, 'student', '权限禁用通知', '您因多次违规被暂停借用权限7天', 'system', '/student/profile', 1, '2026-01-15 09:00:00'),
(1, 'student', '新公告发布', '实验室开放时间调整通知', 'announcement', '/student/dashboard', 0, '2026-01-10 09:00:00'),
(4, 'student', '借用登记成功', '您已成功借用高压反应釜，应还时间：2026-01-18 08:00', 'borrow', '/student/borrows', 1, '2026-01-15 08:00:00'),
(6, 'student', '归还确认', '您借用的pH计已归还，状态良好', 'borrow', '/student/borrows', 1, '2026-01-17 10:00:00');

-- ============================================
-- 13. 插入系统设置数据
-- ============================================
INSERT INTO `system_setting` (`setting_key`, `setting_value`, `description`) VALUES
('lab_info', '{"name":"生物化学综合实验室","introduction":"本校生物化学综合实验室成立于2020年，配备先进仪器设备50余台，服务于生物技术和化学工程专业教学科研。","openHours":[{"days":"周一至周五","time":"08:00-21:00"},{"days":"周六","time":"09:00-17:00"}],"rules":"1.进入实验室必须穿着实验服\\n2.严禁在实验室内饮食\\n3.使用设备前必须接受培训\\n4.实验结束后清理工作台\\n5.发现设备故障及时报告"}', '实验室基本信息'),
('reservation_rules', '{"maxDuration":8,"maxAdvanceDays":7,"cancelAdvanceHours":24,"maxBorrowCount":3,"slotGranularity":2}', '预约规则配置'),
('reminder_settings', '{"overdueThreshold":24,"remindMethods":["sms","inapp"],"remindInterval":"daily","advanceRemindHours":2}', '归还提醒配置'),
('system_version', '"v1.0.0"', '系统版本号');
