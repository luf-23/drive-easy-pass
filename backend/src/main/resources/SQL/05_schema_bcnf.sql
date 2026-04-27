-- ========================================
-- BCNF 优化后的数据库表结构
-- ========================================
-- 优化说明：
-- 1. 消除所有非主属性对候选键的部分依赖和传递依赖
-- 2. 将枚举值提取为独立的字典表
-- 3. 确保每个决定因素都是候选键
-- ========================================

SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

USE drive_easy_pass;

-- ========================================
-- 1. 用户与权限模块（已符合 BCNF）
-- ========================================

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NOT NULL DEFAULT '',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_roles_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_user_role_role
    FOREIGN KEY (role_id) REFERENCES roles (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_routes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  path VARCHAR(120) NOT NULL,
  name VARCHAR(80) NOT NULL,
  title VARCHAR(80) NOT NULL,
  parent_id BIGINT NULL,
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
  role_id BIGINT NOT NULL,
  route_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, route_id),
  CONSTRAINT fk_role_route_role
    FOREIGN KEY (role_id) REFERENCES roles (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_role_route_route
    FOREIGN KEY (route_id) REFERENCES app_routes (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 2. 题库模块（已符合 BCNF）
-- ========================================

CREATE TABLE IF NOT EXISTS questions (
  id BIGINT PRIMARY KEY,
  content VARCHAR(500) NOT NULL,
  option_a VARCHAR(255) NOT NULL,
  option_b VARCHAR(255) NOT NULL,
  option_c VARCHAR(255) NOT NULL,
  option_d VARCHAR(255) NOT NULL,
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

-- ========================================
-- 3. 招生管理模块（BCNF 优化）
-- ========================================

-- 3.1 线索来源字典表（新增）
-- 消除 enroll_leads 表中 source 字段的传递依赖
CREATE TABLE IF NOT EXISTS lead_sources (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NOT NULL DEFAULT '',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_lead_sources_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2 意向等级字典表（新增）
-- 消除 enroll_leads 表中 intent_level 字段的传递依赖
CREATE TABLE IF NOT EXISTS intent_levels (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(20) NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NOT NULL DEFAULT '',
  priority INT NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_intent_levels_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.3 线索状态字典表（新增）
-- 消除 enroll_leads 表中 status 字段的传递依赖
CREATE TABLE IF NOT EXISTS lead_statuses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(20) NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NOT NULL DEFAULT '',
  is_final TINYINT(1) NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_lead_statuses_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.4 跟进类型字典表（新增）
-- 消除 enroll_follow_ups 表中 follow_type 字段的传递依赖
CREATE TABLE IF NOT EXISTS follow_types (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(20) NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NOT NULL DEFAULT '',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_follow_types_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.5 线索主表（优化）
-- 将枚举字段改为外键引用字典表
CREATE TABLE IF NOT EXISTS enroll_leads (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  source_id BIGINT NOT NULL,
  intent_level_id BIGINT NOT NULL,
  status_id BIGINT NOT NULL,
  owner_user_id BIGINT NULL,
  next_follow_time DATETIME NULL,
  remark VARCHAR(500) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_enroll_leads_phone (phone),
  KEY idx_enroll_leads_status (status_id),
  KEY idx_enroll_leads_owner (owner_user_id),
  KEY idx_enroll_leads_create_time (create_time),
  CONSTRAINT fk_enroll_leads_source
    FOREIGN KEY (source_id) REFERENCES lead_sources (id),
  CONSTRAINT fk_enroll_leads_intent
    FOREIGN KEY (intent_level_id) REFERENCES intent_levels (id),
  CONSTRAINT fk_enroll_leads_status
    FOREIGN KEY (status_id) REFERENCES lead_statuses (id),
  CONSTRAINT fk_enroll_leads_owner
    FOREIGN KEY (owner_user_id) REFERENCES users (id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.6 跟进记录表（优化）
-- 将 follow_type 改为外键引用字典表
CREATE TABLE IF NOT EXISTS enroll_follow_ups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  lead_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  follow_type_id BIGINT NOT NULL,
  next_follow_time DATETIME NULL,
  creator_user_id BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_enroll_follow_ups_lead (lead_id),
  KEY idx_enroll_follow_ups_creator (creator_user_id),
  KEY idx_enroll_follow_ups_type (follow_type_id),
  CONSTRAINT fk_enroll_follow_ups_lead
    FOREIGN KEY (lead_id) REFERENCES enroll_leads (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_enroll_follow_ups_type
    FOREIGN KEY (follow_type_id) REFERENCES follow_types (id),
  CONSTRAINT fk_enroll_follow_ups_creator
    FOREIGN KEY (creator_user_id) REFERENCES users (id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 4. 初始化字典数据
-- ========================================

-- 4.1 初始化线索来源数据
INSERT INTO lead_sources (code, name, description) VALUES
('online', '线上渠道', '包括搜索引擎、社交媒体等'),
('offline', '线下渠道', '包括门店咨询、地推等'),
('referral', '转介绍', '老学员推荐'),
('advertisement', '广告投放', '各类广告宣传'),
('other', '其他', '其他来源渠道')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 4.2 初始化意向等级数据
INSERT INTO intent_levels (code, name, description, priority) VALUES
('high', '高', '意向强烈，近期可能报名', 3),
('medium', '中', '有意向，需要考虑', 2),
('low', '低', '意向较弱，需长期跟进', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 4.3 初始化线索状态数据
INSERT INTO lead_statuses (code, name, description, is_final, sort_order) VALUES
('new', '新线索', '刚刚获取的线索', 0, 1),
('following', '跟进中', '正在跟进沟通', 0, 2),
('intention', '有意向', '表达明确意向', 0, 3),
('enrolled', '已报名', '已完成报名', 1, 4),
('lost', '已流失', '确认不报名', 1, 5)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 4.4 初始化跟进类型数据
INSERT INTO follow_types (code, name, description) VALUES
('phone', '电话', '电话跟进'),
('wechat', '微信', '微信沟通'),
('visit', '到访', '到店咨询'),
('sms', '短信', '短信通知'),
('other', '其他', '其他方式')
ON DUPLICATE KEY UPDATE name=VALUES(name);
