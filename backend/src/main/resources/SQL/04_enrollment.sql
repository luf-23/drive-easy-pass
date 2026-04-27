SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

CREATE TABLE IF NOT EXISTS enroll_leads (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  source VARCHAR(30) NOT NULL DEFAULT '其他',
  intent_level VARCHAR(20) NOT NULL DEFAULT '中',
  status VARCHAR(20) NOT NULL DEFAULT '新线索',
  owner_user_id BIGINT NULL,
  next_follow_time DATETIME NULL,
  remark VARCHAR(500) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_enroll_leads_phone (phone),
  KEY idx_enroll_leads_status (status),
  KEY idx_enroll_leads_owner (owner_user_id),
  KEY idx_enroll_leads_create_time (create_time),
  CONSTRAINT fk_enroll_leads_owner
    FOREIGN KEY (owner_user_id) REFERENCES users (id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS enroll_follow_ups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  lead_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  follow_type VARCHAR(20) NOT NULL DEFAULT '电话',
  next_follow_time DATETIME NULL,
  creator_user_id BIGINT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_enroll_follow_ups_lead (lead_id),
  KEY idx_enroll_follow_ups_creator (creator_user_id),
  CONSTRAINT fk_enroll_follow_ups_lead
    FOREIGN KEY (lead_id) REFERENCES enroll_leads (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_enroll_follow_ups_creator
    FOREIGN KEY (creator_user_id) REFERENCES users (id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
