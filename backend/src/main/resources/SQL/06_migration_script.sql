-- ========================================
-- 数据迁移脚本：从旧表结构迁移到 BCNF 新表结构
-- ========================================
-- 使用说明：
-- 1. 确保已经执行了 05_schema_bcnf.sql 创建了新表和字典表
-- 2. 如果有历史数据，执行此脚本进行迁移
-- 3. 验证数据无误后，再删除旧表
-- ========================================

SET NAMES utf8mb4;
USE drive_easy_pass;

-- ========================================
-- 步骤 1：创建临时备份表（保留旧数据结构）
-- ========================================

CREATE TABLE IF NOT EXISTS enroll_leads_backup AS SELECT * FROM enroll_leads;
CREATE TABLE IF NOT EXISTS enroll_follow_ups_backup AS SELECT * FROM enroll_follow_ups;

-- ========================================
-- 步骤 2：迁移 enroll_leads 数据到新结构
-- ========================================

-- 2.1 创建新的线索表（带 _new 后缀）
CREATE TABLE IF NOT EXISTS enroll_leads_new (
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

-- 2.2 从旧表迁移数据到新表（通过字典表映射）
INSERT INTO enroll_leads_new (
  id, name, phone, source_id, intent_level_id, status_id,
  owner_user_id, next_follow_time, remark, create_time, update_time
)
SELECT 
  old.id,
  old.name,
  old.phone,
  -- 映射 source 到 source_id
  COALESCE(ls.id, (SELECT id FROM lead_sources WHERE code = 'other' LIMIT 1)) AS source_id,
  -- 映射 intent_level 到 intent_level_id
  COALESCE(il.id, (SELECT id FROM intent_levels WHERE code = 'medium' LIMIT 1)) AS intent_level_id,
  -- 映射 status 到 status_id
  COALESCE(lst.id, (SELECT id FROM lead_statuses WHERE code = 'new' LIMIT 1)) AS status_id,
  old.owner_user_id,
  old.next_follow_time,
  old.remark,
  old.create_time,
  old.update_time
FROM enroll_leads_backup old
LEFT JOIN lead_sources ls ON ls.code = CASE old.source
  WHEN '线上渠道' THEN 'online'
  WHEN '线下渠道' THEN 'offline'
  WHEN '转介绍' THEN 'referral'
  WHEN '广告投放' THEN 'advertisement'
  WHEN '其他' THEN 'other'
  ELSE 'other'
END
LEFT JOIN intent_levels il ON il.code = CASE old.intent_level
  WHEN '高' THEN 'high'
  WHEN '中' THEN 'medium'
  WHEN '低' THEN 'low'
  ELSE 'medium'
END
LEFT JOIN lead_statuses lst ON lst.code = CASE old.status
  WHEN '新线索' THEN 'new'
  WHEN '跟进中' THEN 'following'
  WHEN '有意向' THEN 'intention'
  WHEN '已报名' THEN 'enrolled'
  WHEN '已流失' THEN 'lost'
  ELSE 'new'
END;

-- ========================================
-- 步骤 3：迁移 enroll_follow_ups 数据到新结构
-- ========================================

-- 3.1 创建新的跟进记录表（带 _new 后缀）
CREATE TABLE IF NOT EXISTS enroll_follow_ups_new (
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
    FOREIGN KEY (lead_id) REFERENCES enroll_leads_new (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_enroll_follow_ups_type
    FOREIGN KEY (follow_type_id) REFERENCES follow_types (id),
  CONSTRAINT fk_enroll_follow_ups_creator
    FOREIGN KEY (creator_user_id) REFERENCES users (id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2 从旧表迁移数据到新表
INSERT INTO enroll_follow_ups_new (
  id, lead_id, content, follow_type_id, next_follow_time, creator_user_id, create_time
)
SELECT 
  old.id,
  old.lead_id,
  old.content,
  -- 映射 follow_type 到 follow_type_id
  COALESCE(ft.id, (SELECT id FROM follow_types WHERE code = 'other' LIMIT 1)) AS follow_type_id,
  old.next_follow_time,
  old.creator_user_id,
  old.create_time
FROM enroll_follow_ups_backup old
LEFT JOIN follow_types ft ON ft.code = CASE old.follow_type
  WHEN '电话' THEN 'phone'
  WHEN '微信' THEN 'wechat'
  WHEN '到访' THEN 'visit'
  WHEN '短信' THEN 'sms'
  WHEN '其他' THEN 'other'
  ELSE 'other'
END;

-- ========================================
-- 步骤 4：验证数据迁移完整性
-- ========================================

-- 4.1 检查线索数量是否一致
SELECT 
  'enroll_leads 总数对比' AS check_item,
  (SELECT COUNT(*) FROM enroll_leads_backup) AS backup_count,
  (SELECT COUNT(*) FROM enroll_leads_new) AS new_count,
  CASE 
    WHEN (SELECT COUNT(*) FROM enroll_leads_backup) = (SELECT COUNT(*) FROM enroll_leads_new) 
    THEN '✅ 一致' 
    ELSE '❌ 不一致' 
  END AS status;

-- 4.2 检查跟进记录数是否一致
SELECT 
  'enroll_follow_ups 总数对比' AS check_item,
  (SELECT COUNT(*) FROM enroll_follow_ups_backup) AS backup_count,
  (SELECT COUNT(*) FROM enroll_follow_ups_new) AS new_count,
  CASE 
    WHEN (SELECT COUNT(*) FROM enroll_follow_ups_backup) = (SELECT COUNT(*) FROM enroll_follow_ups_new) 
    THEN '✅ 一致' 
    ELSE '❌ 不一致' 
  END AS status;

-- 4.3 检查是否有未成功映射的数据
SELECT 
  '检查未映射的 source' AS check_item,
  COUNT(*) AS unmatched_count
FROM enroll_leads_new eln
LEFT JOIN lead_sources ls ON eln.source_id = ls.id
WHERE ls.id IS NULL;

SELECT 
  '检查未映射的 intent_level' AS check_item,
  COUNT(*) AS unmatched_count
FROM enroll_leads_new eln
LEFT JOIN intent_levels il ON eln.intent_level_id = il.id
WHERE il.id IS NULL;

SELECT 
  '检查未映射的 status' AS check_item,
  COUNT(*) AS unmatched_count
FROM enroll_leads_new eln
LEFT JOIN lead_statuses lst ON eln.status_id = lst.id
WHERE lst.id IS NULL;

SELECT 
  '检查未映射的 follow_type' AS check_item,
  COUNT(*) AS unmatched_count
FROM enroll_follow_ups_new efn
LEFT JOIN follow_types ft ON efn.follow_type_id = ft.id
WHERE ft.id IS NULL;

-- ========================================
-- 步骤 5：如果验证通过，替换旧表
-- ========================================

-- ⚠️ 注意：执行以下步骤前，请确认数据验证全部通过！

-- 5.1 删除旧表
DROP TABLE IF EXISTS enroll_leads;
DROP TABLE IF EXISTS enroll_follow_ups;

-- 5.2 重命名新表为正式表名
RENAME TABLE enroll_leads_new TO enroll_leads;
RENAME TABLE enroll_follow_ups_new TO enroll_follow_ups;

-- 5.3 （可选）如果需要彻底清理，可以删除备份表
-- DROP TABLE IF EXISTS enroll_leads_backup;
-- DROP TABLE IF EXISTS enroll_follow_ups_backup;

-- ========================================
-- 完成提示
-- ========================================
SELECT '✅ 数据迁移完成！' AS message;
SELECT '请检查应用代码是否已更新以适配新的表结构' AS reminder;
