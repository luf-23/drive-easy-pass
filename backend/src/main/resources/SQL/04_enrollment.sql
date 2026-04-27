SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

USE drive_easy_pass;

CREATE TABLE IF NOT EXISTS lead_sources (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NOT NULL DEFAULT '',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_lead_sources_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE IF NOT EXISTS follow_types (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(20) NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255) NOT NULL DEFAULT '',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_follow_types_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  KEY idx_enroll_leads_source (source_id),
  KEY idx_enroll_leads_intent_level (intent_level_id),
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

CREATE TABLE IF NOT EXISTS enroll_follow_ups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  lead_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  follow_type_id BIGINT NOT NULL,
  next_follow_time DATETIME NULL,
  creator_user_id BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_enroll_follow_ups_lead (lead_id),
  KEY idx_enroll_follow_ups_type (follow_type_id),
  KEY idx_enroll_follow_ups_creator (creator_user_id),
  KEY idx_enroll_follow_ups_create_time (create_time),
  CONSTRAINT fk_enroll_follow_ups_lead
    FOREIGN KEY (lead_id) REFERENCES enroll_leads (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_enroll_follow_ups_type
    FOREIGN KEY (follow_type_id) REFERENCES follow_types (id),
  CONSTRAINT fk_enroll_follow_ups_creator
    FOREIGN KEY (creator_user_id) REFERENCES users (id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO lead_sources (id, code, name, description, enabled) VALUES
(1, 'online', '线上渠道', '搜索引擎、社交媒体、官网表单等线上来源', 1),
(2, 'offline', '线下渠道', '门店咨询、地推活动等线下来源', 1),
(3, 'referral', '转介绍', '老学员或合作伙伴推荐', 1),
(4, 'advertisement', '广告投放', '线上或线下广告投放获客', 1),
(5, 'other', '其他', '未归类的其他来源', 1)
ON DUPLICATE KEY UPDATE
  code = VALUES(code),
  name = VALUES(name),
  description = VALUES(description),
  enabled = VALUES(enabled);

INSERT INTO intent_levels (id, code, name, description, priority, enabled) VALUES
(1, 'high', '高', '意向强烈，近期可能报名', 3, 1),
(2, 'medium', '中', '有明确兴趣，需要继续跟进', 2, 1),
(3, 'low', '低', '意向较弱，需要长期培育', 1, 1)
ON DUPLICATE KEY UPDATE
  code = VALUES(code),
  name = VALUES(name),
  description = VALUES(description),
  priority = VALUES(priority),
  enabled = VALUES(enabled);

INSERT INTO lead_statuses (id, code, name, description, is_final, sort_order, enabled) VALUES
(1, 'new', '新线索', '刚刚获取，尚未开始跟进', 0, 1, 1),
(2, 'following', '跟进中', '正在持续沟通跟进', 0, 2, 1),
(3, 'intention', '有意向', '已经表达明确报名意向', 0, 3, 1),
(4, 'enrolled', '已报名', '已经完成报名转化', 1, 4, 1),
(5, 'lost', '已流失', '确认不再报名或无法继续跟进', 1, 5, 1)
ON DUPLICATE KEY UPDATE
  code = VALUES(code),
  name = VALUES(name),
  description = VALUES(description),
  is_final = VALUES(is_final),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);

INSERT INTO follow_types (id, code, name, description, enabled) VALUES
(1, 'phone', '电话', '电话沟通跟进', 1),
(2, 'wechat', '微信', '微信沟通跟进', 1),
(3, 'visit', '到访', '到店咨询或现场接待', 1),
(4, 'sms', '短信', '短信通知或提醒', 1),
(5, 'other', '其他', '其他跟进方式', 1)
ON DUPLICATE KEY UPDATE
  code = VALUES(code),
  name = VALUES(name),
  description = VALUES(description),
  enabled = VALUES(enabled);
