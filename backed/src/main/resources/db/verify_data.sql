-- ============================================
-- 数据库验证脚本
-- 用于检查数据是否正确导入
-- ============================================

SELECT '========================================' AS '';
SELECT '实验室设备管理系统 - 数据验证报告' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';

-- 1. 统计各表记录数
SELECT '【表记录统计】' AS '检查项';
SELECT 
    'student' AS '表名', COUNT(*) AS '记录数' FROM student
UNION ALL
SELECT 'teacher', COUNT(*) FROM teacher
UNION ALL
SELECT 'device_category', COUNT(*) FROM device_category
UNION ALL
SELECT 'device', COUNT(*) FROM device
UNION ALL
SELECT 'device_image', COUNT(*) FROM device_image
UNION ALL
SELECT 'reservation', COUNT(*) FROM reservation
UNION ALL
SELECT 'borrow_record', COUNT(*) FROM borrow_record
UNION ALL
SELECT 'violation', COUNT(*) FROM violation
UNION ALL
SELECT 'repair_record', COUNT(*) FROM repair_record
UNION ALL
SELECT 'scrap_record', COUNT(*) FROM scrap_record
UNION ALL
SELECT 'announcement', COUNT(*) FROM announcement
UNION ALL
SELECT 'notification', COUNT(*) FROM notification
UNION ALL
SELECT 'system_setting', COUNT(*) FROM system_setting;

SELECT '' AS '';

-- 2. 设备状态分布
SELECT '【设备状态分布】' AS '检查项';
SELECT 
    status AS '状态',
    COUNT(*) AS '数量',
    CONCAT(ROUND(COUNT(*) * 100 / (SELECT COUNT(*) FROM device), 2), '%') AS '占比'
FROM device
GROUP BY status
ORDER BY COUNT(*) DESC;

SELECT '' AS '';

-- 3. 预约状态分布
SELECT '【预约状态分布】' AS '检查项';
SELECT 
    status AS '状态',
    COUNT(*) AS '数量'
FROM reservation
GROUP BY status
ORDER BY COUNT(*) DESC;

SELECT '' AS '';

-- 4. 借用记录状态分布
SELECT '【借用记录状态分布】' AS '检查项';
SELECT 
    status AS '状态',
    COUNT(*) AS '数量',
    SUM(is_overdue) AS '超时数量'
FROM borrow_record
GROUP BY status;

SELECT '' AS '';

-- 5. 学生违规统计
SELECT '【学生违规统计TOP5】' AS '检查项';
SELECT 
    s.name AS '学生姓名',
    s.student_no AS '学号',
    COUNT(v.id) AS '违规次数',
    GROUP_CONCAT(DISTINCT v.type SEPARATOR ', ') AS '违规类型'
FROM student s
LEFT JOIN violation v ON s.id = v.student_id AND v.status = 1
GROUP BY s.id, s.name, s.student_no
HAVING COUNT(v.id) > 0
ORDER BY COUNT(v.id) DESC
LIMIT 5;

SELECT '' AS '';

-- 6. 实验室类型分布
SELECT '【学生实验室类型分布】' AS '检查项';
SELECT 
    lab_type AS '实验室类型',
    CASE lab_type 
        WHEN 'bio' THEN '生物实验室'
        WHEN 'chem' THEN '化学实验室'
    END AS '类型名称',
    COUNT(*) AS '人数'
FROM student
GROUP BY lab_type;

SELECT '' AS '';

-- 7. 公告列表
SELECT '【最新公告（前5条）】' AS '检查项';
SELECT 
    id AS 'ID',
    title AS '标题',
    target_type AS '发布范围',
    is_pinned AS '是否置顶',
    publish_time AS '发布时间'
FROM announcement
WHERE status = 1
ORDER BY is_pinned DESC, publish_time DESC
LIMIT 5;

SELECT '' AS '';

-- 8. 未读通知统计
SELECT '【未读通知统计】' AS '检查项';
SELECT 
    user_type AS '用户类型',
    COUNT(*) AS '未读数量'
FROM notification
WHERE is_read = 0
GROUP BY user_type;

SELECT '' AS '';

-- 9. 设备分类层级
SELECT '【设备分类结构】' AS '检查项';
SELECT 
    dc1.name AS '一级分类',
    dc2.name AS '二级分类',
    dc2.lab_type AS '实验室类型'
FROM device_category dc1
LEFT JOIN device_category dc2 ON dc1.id = dc2.parent_id
WHERE dc1.parent_id = 0
ORDER BY dc1.sort_order, dc2.sort_order;

SELECT '' AS '';

-- 10. 维修费用统计
SELECT '【维修费用统计】' AS '检查项';
SELECT 
    d.name AS '设备名称',
    r.repair_date AS '维修日期',
    r.repair_person AS '维修人员',
    r.cost AS '维修费用',
    r.result AS '维修结果'
FROM repair_record r
JOIN device d ON r.device_id = d.id
ORDER BY r.repair_date DESC;

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT '验证完成！' AS '';
SELECT '========================================' AS '';
