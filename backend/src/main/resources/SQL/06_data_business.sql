SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

INSERT INTO course_packages (code, name, vehicle_type, price, lesson_hours, highlights, tag, sort_no, enabled) VALUES
('C1_STANDARD', 'C1 普通班', 'C1', 3980, 56, '科目一题库练习|科目二场地训练|科目三道路训练|科目四考前辅导', '热门', 1, 1),
('C2_STANDARD', 'C2 自动挡班', 'C2', 4280, 52, '自动挡训练车|预约提醒|全科目进度跟踪|考试报名协助', '推荐', 2, 1),
('VIP_FAST', 'VIP 速成班', 'C1/C2', 6680, 68, '一对一教练|优先排课|专属跟进|考前强化', 'VIP', 3, 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  vehicle_type = VALUES(vehicle_type),
  price = VALUES(price),
  lesson_hours = VALUES(lesson_hours),
  highlights = VALUES(highlights),
  tag = VALUES(tag),
  sort_no = VALUES(sort_no),
  enabled = VALUES(enabled);