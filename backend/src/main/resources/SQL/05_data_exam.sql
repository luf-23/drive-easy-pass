SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

INSERT INTO exam_venues (id, name, address, contact_phone) VALUES
(1, '市车管所理论考场（城东）', '高新区科苑东路 168 号', '028-12345678'),
(2, '宏远驾校科目二电子化考场', '经开区训练基地 A 区', '028-87654321'),
(3, '顺达科目三社会化考场', '南环路外延线 99 号', '028-88990011')
ON DUPLICATE KEY UPDATE name = VALUES(name), address = VALUES(address), contact_phone = VALUES(contact_phone);

INSERT INTO exam_schedules (id, venue_id, exam_type, exam_date, start_time, end_time, capacity, remark) VALUES
(1, 1, '科目一', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '09:00:00', '11:30:00', 120, '理论考试第一轮'),
(2, 1, '科目一', DATE_ADD(CURDATE(), INTERVAL 6 DAY), '14:00:00', '16:30:00', 120, '理论考试加场'),
(3, 1, '科目四', DATE_ADD(CURDATE(), INTERVAL 10 DAY), '09:00:00', '10:45:00', 80, '安全文明常识'),
(4, 2, '科目二', DATE_ADD(CURDATE(), INTERVAL 7 DAY), '08:00:00', '12:00:00', 40, '场地五项'),
(5, 2, '科目二', DATE_ADD(CURDATE(), INTERVAL 9 DAY), '08:00:00', '12:00:00', 40, ''),
(6, 3, '科目三', DATE_ADD(CURDATE(), INTERVAL 8 DAY), '13:30:00', '17:00:00', 30, '道路驾驶技能'),
(7, 3, '科目三', DATE_ADD(CURDATE(), INTERVAL 11 DAY), '08:30:00', '12:00:00', 30, '')
ON DUPLICATE KEY UPDATE
  venue_id = VALUES(venue_id),
  exam_type = VALUES(exam_type),
  exam_date = VALUES(exam_date),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  capacity = VALUES(capacity),
  remark = VALUES(remark);