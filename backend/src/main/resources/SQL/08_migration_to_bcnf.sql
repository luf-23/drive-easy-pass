-- ========================================
-- 从旧表结构迁移到BCNF优化表结构的迁移脚本
-- ========================================

SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

USE drive_easy_pass;

-- 1. 备份旧数据（如果存在旧表）
CREATE TABLE IF NOT EXISTS enroll_leads_backup AS SELECT * FROM enroll_leads WHERE 1=0;
INSERT INTO enroll_leads_backup SELECT * FROM enroll_leads;

CREATE TABLE IF NOT EXISTS enroll_follow_ups_backup AS SELECT * FROM enroll_follow_ups WHERE 1=0;
INSERT INTO enroll_follow_ups_backup SELECT * FROM enroll_follow_ups;

-- 2. 删除旧表（如果存在字符串字段的旧表结构）
DROP TABLE IF EXISTS enroll_follow_ups;
DROP TABLE IF EXISTS enroll_leads;

-- 3. 创建BCNF优化后的表结构（执行05_schema_bcnf.sql的内容）
-- ========================================
-- BCNF 优化后的数据库表结构
-- ========================================

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

-- 4. 初始化字典数据
INSERT INTO lead_sources (code, name, description) VALUES
('online', '线上渠道', '包括搜索引擎、社交媒体等'),
('offline', '线下渠道', '包括门店咨询、地推等'),
('referral', '转介绍', '老学员推荐'),
('advertisement', '广告投放', '各类广告宣传'),
('other', '其他', '其他来源渠道')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO intent_levels (code, name, description, priority) VALUES
('high', '高', '意向强烈，近期可能报名', 3),
('medium', '中', '有意向，需要考虑', 2),
('low', '低', '意向较弱，需长期跟进', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO lead_statuses (code, name, description, is_final, sort_order) VALUES
('new', '新线索', '刚刚获取的线索', 0, 1),
('following', '跟进中', '正在跟进沟通', 0, 2),
('intention', '有意向', '表达明确意向', 0, 3),
('enrolled', '已报名', '已完成报名', 1, 4),
('lost', '已流失', '确认不报名', 1, 5)
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO follow_types (code, name, description) VALUES
('phone', '电话', '电话跟进'),
('wechat', '微信', '微信沟通'),
('visit', '到访', '到店咨询'),
('sms', '短信', '短信通知'),
('other', '其他', '其他方式')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 5. 如果有旧数据，进行数据迁移（这里假设旧数据使用中文状态值）
-- 注意：这个迁移逻辑需要根据实际的旧数据格式调整
INSERT INTO enroll_leads (id, name, phone, source_id, intent_level_id, status_id, owner_user_id, next_follow_time, remark, create_time, update_time)
SELECT 
    id,
    name,
    phone,
    COALESCE((SELECT id FROM lead_sources WHERE code = 
        CASE 
            WHEN source = '线上广告' THEN 'online'
            WHEN source = '线下渠道' THEN 'offline' 
            WHEN source = '转介绍' THEN 'referral'
            WHEN source = '广告投放' THEN 'advertisement'
            ELSE 'other'
        END
    ), (SELECT id FROM lead_sources WHERE code = 'other')),
    COALESCE((SELECT id FROM intent_levels WHERE code = 
        CASE 
            WHEN intent_level = '高' THEN 'high'
            WHEN intent_level = '中' THEN 'medium'
            WHEN intent_level = '低' THEN 'low'
            ELSE 'medium'
        END
    ), (SELECT id FROM intent_levels WHERE code = 'medium')),
    COALESCE((SELECT id FROM lead_statuses WHERE code = 
        CASE 
            WHEN status = '新线索' THEN 'new'
            WHEN status = '跟进中' THEN 'following'
            WHEN status = '有意向' THEN 'intention'
            WHEN status = '已报名' THEN 'enrolled'
            WHEN status = '已流失' THEN 'lost'
            ELSE 'new'
        END
    ), (SELECT id FROM lead_statuses WHERE code = 'new')),
    owner_user_id,
    next_follow_time,
    remark,
    create_time,
    update_time
FROM enroll_leads_backup
WHERE NOT EXISTS (SELECT 1 FROM enroll_leads WHERE enroll_leads.id = enroll_leads_backup.id);