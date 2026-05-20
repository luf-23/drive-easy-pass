SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

USE drive_easy_pass;

-- ---------------------------------------------------------------------------
-- Mock 业务数据（可重复执行：先清理本脚本写入的固定 ID 范围）
-- 测试账号密码均为：123456（与 03_data_system.sql 中 admin 相同哈希）
-- ---------------------------------------------------------------------------

DELETE FROM enrollment_follow_records WHERE lead_id BETWEEN 101 AND 120;
DELETE FROM enrollment_intents WHERE id BETWEEN 101 AND 120;
DELETE FROM exam_registrations WHERE id BETWEEN 201 AND 220;
DELETE FROM wrong_questions WHERE user_id BETWEEN 4 AND 11;

-- 教练 / 学员账号
INSERT INTO users (id, username, password_hash, nickname, role, email, status) VALUES
(2, 'coach_wang', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '王教练', 'coach', 'coach.wang@example.com', 1),
(3, 'coach_li', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '李教练', 'coach', 'coach.li@example.com', 1),
(4, 'student_zhang', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '张晓明', 'student', 'zhang@example.com', 1),
(5, 'student_liu', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '刘芳', 'student', 'liu@example.com', 1),
(6, 'student_chen', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '陈浩', 'student', 'chen@example.com', 1),
(7, 'student_zhao', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '赵敏', 'student', 'zhao@example.com', 1),
(8, 'student_sun', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '孙磊', 'student', 'sun@example.com', 1),
(9, 'student_zhou', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '周婷', 'student', 'zhou@example.com', 1),
(10, 'student_wu', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '吴强', 'student', 'wu@example.com', 1),
(11, 'student_xu', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '徐静', 'student', 'xu@example.com', 1)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  nickname = VALUES(nickname),
  role = VALUES(role),
  email = VALUES(email),
  status = VALUES(status);

-- 招生线索（含各漏斗阶段，create_time 分散在近 30 天便于统计图表）
INSERT INTO enrollment_intents
  (id, name, phone, source, intent_level, status, owner_user_id, next_follow_time, remark, create_time, update_time)
VALUES
(101, '林佳怡', '13800001001', '抖音', '高', '待跟进', 2, DATE_ADD(NOW(), INTERVAL 1 DAY), '咨询 C1 普通班', DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(102, '黄俊杰', '13800001002', '线上广告', '中', '已联系', 2, DATE_ADD(NOW(), INTERVAL 2 DAY), '已加微信，待到店', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(103, '马婷婷', '13800001003', '转介绍', '高', '有意向', 3, DATE_ADD(NOW(), INTERVAL 1 DAY), '朋友已在学，倾向 VIP 班', DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(104, '张晓明', '13800001004', '门店', '高', '已报名', 2, NULL, '已报名 C1 普通班，当前科目一', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(105, '刘芳', '13800001005', '小红书', '中', '已报名', 2, NULL, '科目二训练中', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(106, '陈浩', '13800001006', '地推', '高', '已报名', 3, NULL, '科目三道路练习', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(107, '赵敏', '13800001007', '其他', '中', '已报名', 3, NULL, '科目四考前冲刺', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(108, '孙磊', '13800001008', '线上广告', '低', '已放弃', 2, NULL, '价格原因放弃', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
(109, '周婷', '13800001009', '抖音', '中', '无效线索', NULL, NULL, '空号', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
(110, '吴强', '13800001010', '转介绍', '高', '已报名', 2, NULL, 'VIP 速成班，科目一已通过', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(111, '徐静', '13800001011', '门店', '中', '已联系', 3, DATE_ADD(NOW(), INTERVAL 3 DAY), '周末到店体验', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
(112, '何鹏', '13800001012', '地推', '高', '待跟进', NULL, DATE_ADD(NOW(), INTERVAL 1 DAY), '现场登记，待分配教练', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(113, '邓雪', '13800001013', '小红书', '中', '有意向', 2, DATE_ADD(NOW(), INTERVAL 2 DAY), '对比 C2 自动挡', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
(114, '罗斌', '13800001014', '线上广告', '低', '已联系', 3, DATE_ADD(NOW(), INTERVAL 4 DAY), '仅咨询费用', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(115, '韩雪', '13800001015', '抖音', '高', '已报名', 3, NULL, '新报名学员，科目一待考', CURDATE(), NOW()),
(116, '冯涛', '13800001016', '新线索', '中', '新线索', NULL, DATE_ADD(NOW(), INTERVAL 1 DAY), '官网留资', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(117, '曹丽', '13800001017', '门店', '中', '已到访', 2, DATE_ADD(NOW(), INTERVAL 2 DAY), '到店参观场地', DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(118, '袁杰', '13800001018', '转介绍', '高', '已报名', 2, NULL, '科目二倒库练习中', DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(119, '丁玲', '13800001019', '其他', '低', '无效', NULL, NULL, '重复咨询', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(120, '蒋伟', '13800001020', '地推', '中', '待跟进', 3, DATE_ADD(NOW(), INTERVAL 1 DAY), '晚间活动登记', DATE_SUB(NOW(), INTERVAL 4 DAY), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  phone = VALUES(phone),
  source = VALUES(source),
  intent_level = VALUES(intent_level),
  status = VALUES(status),
  owner_user_id = VALUES(owner_user_id),
  next_follow_time = VALUES(next_follow_time),
  remark = VALUES(remark),
  create_time = VALUES(create_time),
  update_time = VALUES(update_time);

-- 跟进记录
INSERT INTO enrollment_follow_records
  (id, lead_id, content, follow_type, next_follow_time, creator_user_id, create_time)
VALUES
(301, 102, '电话沟通学车时间安排，客户表示下周可到校', '电话', DATE_ADD(NOW(), INTERVAL 3 DAY), 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(302, 103, '发送班型对比表，客户对 VIP 班感兴趣', '微信', DATE_ADD(NOW(), INTERVAL 2 DAY), 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(304, 104, '到店签约 C1 普通班，收取定金', '到店', NULL, 2, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(305, 111, '确认周末体验课名额', '电话', DATE_ADD(NOW(), INTERVAL 3 DAY), 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(306, 113, '介绍 C2 自动挡训练车与排课规则', '微信', DATE_ADD(NOW(), INTERVAL 2 DAY), 2, NOW()),
(307, 115, '完成报名登记，安排科目一模考', '到店', NULL, 3, CURDATE())
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  follow_type = VALUES(follow_type),
  next_follow_time = VALUES(next_follow_time),
  creator_user_id = VALUES(creator_user_id),
  create_time = VALUES(create_time);

-- 考试预约 / 成绩（依赖 05_data_exam.sql 中的场次 id 1-7）
INSERT INTO exam_registrations
  (id, user_id, schedule_id, status, score, passed, remark, create_time, update_time)
VALUES
(201, 4, 1, 'pending', NULL, NULL, '科目一首次预约，待审核', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(202, 5, 1, 'approved', NULL, NULL, '科目一加场预约', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(203, 6, 4, 'approved', NULL, NULL, '科目二场地考试', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(204, 7, 3, 'completed', 92, 'Y', '科目四已通过', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(205, 8, 6, 'rejected', NULL, NULL, '资料不全驳回', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(206, 10, 2, 'completed', 88, 'Y', '科目一模考通过', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
(207, 10, 4, 'completed', 78, 'N', '科目二未通过，需补考', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(208, 11, 7, 'pending', NULL, NULL, '科目三预约待审', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(209, 6, 7, 'cancelled', NULL, NULL, '学员主动取消', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(210, 5, 5, 'completed', 95, 'Y', '科目二补考通过', DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY))
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  schedule_id = VALUES(schedule_id),
  status = VALUES(status),
  score = VALUES(score),
  passed = VALUES(passed),
  remark = VALUES(remark),
  create_time = VALUES(create_time),
  update_time = VALUES(update_time);

-- 学员错题本样例
INSERT INTO wrong_questions (user_id, question_id, create_time) VALUES
(4, 3, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 12, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 7, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 22, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(10, 36, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 41, NOW())
ON DUPLICATE KEY UPDATE create_time = VALUES(create_time);
