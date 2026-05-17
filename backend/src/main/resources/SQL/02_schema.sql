SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  role ENUM('student', 'coach', 'admin') NOT NULL DEFAULT 'student',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS questions (
  id BIGINT PRIMARY KEY,
  content VARCHAR(500) NOT NULL,
  option_a VARCHAR(255) NOT NULL,
  option_b VARCHAR(255) NOT NULL,
  option_c VARCHAR(255) NOT NULL,
  option_d VARCHAR(255) NOT NULL,
  exam_type VARCHAR(20) NOT NULL DEFAULT '科目一',
  answer CHAR(1) NOT NULL,
  explanation VARCHAR(500) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wrong_questions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_wrong_user_question (user_id, question_id),
  CONSTRAINT fk_wrong_question_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_wrong_question_question
    FOREIGN KEY (question_id) REFERENCES questions (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_routes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  path VARCHAR(120) NOT NULL,
  name VARCHAR(80) NOT NULL,
  title VARCHAR(80) NOT NULL,
  parent_id BIGINT NULL,
  redirect VARCHAR(120) NOT NULL DEFAULT '',
  component VARCHAR(160) NOT NULL DEFAULT '',
  icon VARCHAR(80) NOT NULL DEFAULT '',
  rank_no INT NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_app_routes_path (path),
  CONSTRAINT fk_app_route_parent
    FOREIGN KEY (parent_id) REFERENCES app_routes (id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS role_routes (
  role ENUM('student', 'admin') NOT NULL,
  route_id BIGINT NOT NULL,
  PRIMARY KEY (role, route_id),
  CONSTRAINT fk_role_route_route
    FOREIGN KEY (route_id) REFERENCES app_routes (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_venues (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NOT NULL DEFAULT '',
  contact_phone VARCHAR(40) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_schedules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  venue_id BIGINT NOT NULL,
  exam_type VARCHAR(20) NOT NULL,
  exam_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  capacity INT NOT NULL DEFAULT 60,
  remark VARCHAR(255) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_exam_schedule_venue
    FOREIGN KEY (venue_id) REFERENCES exam_venues (id)
    ON DELETE CASCADE,
  UNIQUE KEY uk_exam_schedule_natural (
    venue_id,
    exam_type,
    exam_date,
    start_time
  ),
  INDEX idx_exam_schedule_type_date (exam_type, exam_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_registrations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  schedule_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'pending',
  score INT NULL,
  passed ENUM('Y', 'N') NULL,
  remark VARCHAR(255) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_registration_user_schedule (user_id, schedule_id),
  INDEX idx_registration_user_status (user_id, status),
  CONSTRAINT fk_registration_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_registration_schedule
    FOREIGN KEY (schedule_id) REFERENCES exam_schedules (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS course_packages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(40) NOT NULL,
  name VARCHAR(100) NOT NULL,
  vehicle_type VARCHAR(20) NOT NULL DEFAULT 'C1',
  price INT NOT NULL DEFAULT 0,
  lesson_hours INT NOT NULL DEFAULT 0,
  highlights VARCHAR(1000) NOT NULL DEFAULT '',
  tag VARCHAR(40) NOT NULL DEFAULT '',
  sort_no INT NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_course_packages_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS enrollment_intents (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  source VARCHAR(50) NOT NULL DEFAULT '其他',
  intent_level VARCHAR(20) NOT NULL DEFAULT '中',
  status VARCHAR(30) NOT NULL DEFAULT '待跟进',
  owner_user_id BIGINT NULL,
  next_follow_time DATETIME NULL,
  remark VARCHAR(500) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_enrollment_status (status),
  INDEX idx_enrollment_phone (phone),
  INDEX idx_enrollment_owner (owner_user_id),
  CONSTRAINT fk_enrollment_owner
    FOREIGN KEY (owner_user_id) REFERENCES users (id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS enrollment_follow_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  lead_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  follow_type VARCHAR(30) NOT NULL DEFAULT '电话',
  next_follow_time DATETIME NULL,
  creator_user_id BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_follow_lead (lead_id, create_time),
  CONSTRAINT fk_follow_lead
    FOREIGN KEY (lead_id) REFERENCES enrollment_intents (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_follow_creator
    FOREIGN KEY (creator_user_id) REFERENCES users (id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
