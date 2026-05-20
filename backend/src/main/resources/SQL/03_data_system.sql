SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

-- 初始化管理员账号 (密码: 123456)
INSERT INTO users (id, username, password_hash, nickname, role, email, status) VALUES
(1, 'admin', 'pbkdf2$120000$EVOn7yO1zx01sx92FLdEuA==$4s6EZZodzicFN2xcIndwnXBXDRz7/y1Cp0L375csNFg=', '系统管理员', 'admin', 'admin@example.com', 1)
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), nickname = VALUES(nickname), role = VALUES(role);

-- 路由菜单
INSERT INTO app_routes (id, path, name, title, parent_id, redirect, component, icon, rank_no, enabled) VALUES
(1, '/drive', 'DriveBusiness', '驾考学习', NULL, '/home', 'Layout', 'ep/guide', 1, 1),
(2, '/home', 'DriveHome', '学员首页', 1, '', 'HomeView', 'ep/home-filled', 1, 1),
(3, '/practice', 'Practice', '理论练习', 1, '', 'PracticeView', 'ep/edit-pen', 2, 1),
(4, '/exam', 'Exam', '模拟考试', 1, '', 'ExamView', 'ep/document-checked', 3, 1),
(5, '/wrong', 'WrongQuestions', '错题本', 1, '', 'WrongQuestionsView', 'ep/notebook', 4, 1),
(10, '/operation', 'OperationCenter', '驾校运营后台', NULL, '/operation/dashboard', 'Layout', 'ep/data-board', 10, 1),
(101, '/operation/dashboard', 'AdminDashboard', '工作台', 10, '', 'WelcomeView', 'ep/monitor', 1, 1),
(110, '/operation/students', 'StudentCenter', '学员管理', 10, '/operation/students/list', '', 'ep/user', 2, 1),
(111, '/operation/students/list', 'StudentList', '学员列表', 110, '', '', 'ep/list', 1, 1),
(112, '/operation/students/progress', 'StudentProgress', '学员考试进度', 110, '', '', 'ep/odometer', 2, 1),
(115, '/operation/coaches', 'CoachManagement', '教练管理', 10, '', '', 'ep/coordinate', 3, 1),
(11, '/operation/enrollment', 'EnrollmentCenter', '招生管理', 10, '/operation/enrollment/intents', '', 'ep/user-filled', 4, 1),
(121, '/operation/enrollment/intents', 'Enrollment', '报名意向', 11, '', 'EnrollmentManagementView', 'ep/chat-dot-round', 1, 1),
(122, '/operation/enrollment/packages', 'CoursePackageManagement', '课程套餐', 11, '', '', 'ep/goods', 2, 1),
(130, '/operation/questions', 'QuestionBank', '题库管理', 10, '/operation/questions/subject1', '', 'ep/notebook', 5, 1),
(131, '/operation/questions/subject1', 'SubjectOneQuestions', '科目一题库', 130, '', '', 'ep/document', 1, 1),
(132, '/operation/questions/subject4', 'SubjectFourQuestions', '科目四题库', 130, '', '', 'ep/document-copy', 2, 1),
(140, '/operation/exams', 'ExamManagement', '考试管理', 10, '/operation/exams/registrations', '', 'ep/tickets', 6, 1),
(141, '/operation/exams/rooms', 'ExamRoomManagement', '考场管理', 140, '', '', 'ep/location', 1, 1),
(142, '/operation/exams/schedules', 'ExamScheduleManagement', '考试场次', 140, '', '', 'ep/calendar', 2, 1),
(143, '/operation/exams/registrations', 'ExamService', '预约审核', 140, '', 'ExamServiceManagementView', 'ep/select', 3, 1),
(144, '/operation/exams/results', 'ExamResultManagement', '成绩管理', 140, '', 'ExamServiceManagementView', 'ep/checked', 4, 1),
(150, '/operation/statistics', 'StatisticsCenter', '统计分析', 10, '', 'operation/statistics/index', 'ep/data-analysis', 7, 1),
(160, '/operation/system', 'SystemManagement', '系统管理', 10, '/operation/system/users', '', 'ep/setting', 8, 1),
(161, '/operation/system/users', 'UserManagement', '用户管理', 160, '', 'UserManagementView', 'ep/user-filled', 1, 1),
(16, '/operation/system/routes', 'RouteManagement', '路由管理', 160, '', 'RouteManagementView', 'ep/menu', 2, 1),
(17, '/operation/system/roles', 'RoleManagement', '角色管理', 160, '', 'RoleManagementView', 'ep/avatar', 3, 1),
(20, '/service', 'ServicePortal', '报名服务', NULL, '/service/home', 'ServiceLayout', 'ep/shop', 2, 1),
(21, '/service/home', 'ServiceHome', '驾校首页', 20, '', 'ServiceHomeView', 'ep/home-filled', 1, 1),
(23, '/service/signup', 'ServiceSignup', '在线报名', 20, '', 'ServiceSignupView', 'ep/edit-pen', 3, 1),
(24, '/service/profile', 'ServiceProfile', '个人中心', 20, '', 'ServiceProfileView', 'ep/user', 4, 1),
(25, '/service/exam-booking', 'ServiceExamBooking', '考试预约（科目一至科目四）', 20, '', 'ServiceExamBookingView', 'ep/tickets', 2, 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  parent_id = VALUES(parent_id),
  redirect = VALUES(redirect),
  component = VALUES(component),
  icon = VALUES(icon),
  rank_no = VALUES(rank_no),
  enabled = VALUES(enabled);

-- 角色菜单权限
DELETE FROM role_routes WHERE role IN ('admin', 'coach', 'student');

INSERT INTO role_routes (role, route_id) VALUES
('admin', 10),
('admin', 101),
('admin', 110),
('admin', 111),
('admin', 112),
('admin', 115),
('admin', 11),
('admin', 121),
('admin', 122),
('admin', 130),
('admin', 131),
('admin', 132),
('admin', 140),
('admin', 141),
('admin', 142),
('admin', 143),
('admin', 144),
('admin', 150),
('admin', 160),
('admin', 161),
('admin', 16),
('admin', 17),
('coach', 10),
('coach', 101),
('coach', 110),
('coach', 112),
('coach', 115),
('coach', 140),
('coach', 142),
('coach', 143),
('student', 1),
('student', 2),
('student', 3),
('student', 4),
('student', 5),
('student', 20),
('student', 21),
('student', 23),
('student', 24),
('student', 25);