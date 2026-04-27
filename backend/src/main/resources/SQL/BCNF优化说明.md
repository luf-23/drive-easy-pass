# BCNF（博伊科德范式）优化说明

## 一、什么是 BCNF？

BCNF（Boyce-Codd Normal Form）是数据库规范化的一个重要范式，它要求：
1. 首先满足 3NF
2. **每个决定因素都必须是候选键**

简单来说，BCNF 消除了所有属性对键的部分依赖和传递依赖，确保数据的一致性。

---

## 二、原表结构存在的问题

### 问题 1：enroll_leads 表的传递依赖

**原表结构：**
```sql
CREATE TABLE enroll_leads (
  id BIGINT PRIMARY KEY,
  name VARCHAR(50),
  phone VARCHAR(20),
  source VARCHAR(30),           -- ❌ 存在传递依赖
  intent_level VARCHAR(20),     -- ❌ 存在传递依赖
  status VARCHAR(20),           -- ❌ 存在传递依赖
  owner_user_id BIGINT,
  ...
)
```

**问题分析：**
- `source`（来源）：存储的是"线上渠道"、"线下渠道"等文本值
  - 依赖关系：`id → source`，但 `source` 不是候选键
  - 如果要修改某个来源的名称，需要更新多条记录
  
- `intent_level`（意向等级）：存储"高"、"中"、"低"
  - 依赖关系：`id → intent_level`，但 `intent_level` 不是候选键
  - 无法存储等级的详细描述、优先级等信息
  
- `status`（状态）：存储"新线索"、"跟进中"等
  - 依赖关系：`id → status`，但 `status` 不是候选键
  - 无法存储状态的流转规则、是否终态等信息

**违反 BCNF 的原因：**
这些字段形成了传递依赖：
```
id → source
source → {name, description}  （隐含的语义依赖）
```

---

### 问题 2：enroll_follow_ups 表的类似问题

**原表结构：**
```sql
CREATE TABLE enroll_follow_ups (
  id BIGINT PRIMARY KEY,
  lead_id BIGINT,
  content VARCHAR(500),
  follow_type VARCHAR(20),   -- ❌ 存在传递依赖
  ...
)
```

**问题分析：**
- `follow_type` 存储"电话"、"微信"等文本
- 同样的传递依赖问题

---

## 三、BCNF 优化方案

### 优化思路

将枚举值提取为独立的字典表，主表只存储外键引用。

### 新增的字典表

#### 1. lead_sources（线索来源字典表）

```sql
CREATE TABLE lead_sources (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL UNIQUE,    -- 业务编码
  name VARCHAR(50) NOT NULL,            -- 显示名称
  description VARCHAR(255),             -- 描述
  enabled TINYINT(1) DEFAULT 1,         -- 是否启用
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
)
```

**符合 BCNF 的原因：**
- 主键 `id` 是唯一标识
- 候选键 `code` 也是唯一的
- 所有非主属性直接依赖于主键，没有传递依赖

#### 2. intent_levels（意向等级字典表）

```sql
CREATE TABLE intent_levels (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  priority INT DEFAULT 0,               -- 优先级，用于排序
  enabled TINYINT(1) DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
)
```

#### 3. lead_statuses（线索状态字典表）

```sql
CREATE TABLE lead_statuses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  is_final TINYINT(1) DEFAULT 0,        -- 是否终态（如"已报名"、"已流失"）
  sort_order INT DEFAULT 0,             -- 排序顺序
  enabled TINYINT(1) DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
)
```

#### 4. follow_types（跟进类型字典表）

```sql
CREATE TABLE follow_types (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  enabled TINYINT(1) DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
)
```

---

### 优化后的主表

#### enroll_leads（线索主表）

```sql
CREATE TABLE enroll_leads (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  source_id BIGINT NOT NULL,              -- ✅ 改为外键
  intent_level_id BIGINT NOT NULL,        -- ✅ 改为外键
  status_id BIGINT NOT NULL,              -- ✅ 改为外键
  owner_user_id BIGINT NULL,
  next_follow_time DATETIME NULL,
  remark VARCHAR(500) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_enroll_leads_phone (phone),
  CONSTRAINT fk_enroll_leads_source
    FOREIGN KEY (source_id) REFERENCES lead_sources (id),
  CONSTRAINT fk_enroll_leads_intent
    FOREIGN KEY (intent_level_id) REFERENCES intent_levels (id),
  CONSTRAINT fk_enroll_leads_status
    FOREIGN KEY (status_id) REFERENCES lead_statuses (id),
  CONSTRAINT fk_enroll_leads_owner
    FOREIGN KEY (owner_user_id) REFERENCES users (id) ON DELETE SET NULL
)
```

**符合 BCNF 的原因：**
- 所有非主属性完全依赖于主键 `id`
- 没有部分依赖（不存在复合主键）
- 没有传递依赖（原来的枚举值已经提取到独立表）
- 每个决定因素都是候选键

#### enroll_follow_ups（跟进记录表）

```sql
CREATE TABLE enroll_follow_ups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  lead_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  follow_type_id BIGINT NOT NULL,         -- ✅ 改为外键
  next_follow_time DATETIME NULL,
  creator_user_id BIGINT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_enroll_follow_ups_lead (lead_id),
  CONSTRAINT fk_enroll_follow_ups_lead
    FOREIGN KEY (lead_id) REFERENCES enroll_leads (id) ON DELETE CASCADE,
  CONSTRAINT fk_enroll_follow_ups_type
    FOREIGN KEY (follow_type_id) REFERENCES follow_types (id),
  CONSTRAINT fk_enroll_follow_ups_creator
    FOREIGN KEY (creator_user_id) REFERENCES users (id) ON DELETE SET NULL
)
```

---

## 四、优化对比

| 项目 | 优化前 | 优化后 |
|------|--------|--------|
| **表数量** | 8 张表 | 12 张表（+4 张字典表） |
| **枚举存储** | 字符串硬编码 | 外键引用字典表 |
| **BCNF 合规** | ❌ 存在传递依赖 | ✅ 完全符合 BCNF |
| **数据一致性** | 容易出现不一致 | 通过外键保证一致性 |
| **扩展性** | 修改枚举需改代码和数据 | 只需在字典表中添加记录 |
| **存储空间** | 重复存储字符串 | 只存储整数 ID |

---

## 五、优化带来的好处

### 1. 消除数据冗余
- **优化前**：每条线索都存储 "线上渠道" 这个字符串
- **优化后**：只存储 source_id = 1，节省存储空间

### 2. 保证数据一致性
- **优化前**：可能出现 "线上"、"线上渠道"、"在线" 等多种写法
- **优化后**：通过外键约束，只能使用字典表中定义的值

### 3. 易于维护和扩展
- **优化前**：新增一个来源类型需要修改代码和更新所有历史数据
- **优化后**：只需在 lead_sources 表中插入一条记录

### 4. 支持更多元数据
- 可以为每个字典项添加描述、启用状态、优先级等额外信息
- 例如：`intent_levels` 表可以存储 `priority` 字段用于排序

### 5. 便于统计分析
```sql
-- 统计各来源的线索数量
SELECT ls.name, COUNT(*) as cnt
FROM enroll_leads el
JOIN lead_sources ls ON el.source_id = ls.id
GROUP BY ls.id, ls.name
ORDER BY cnt DESC;
```

---

## 六、迁移指南

### 步骤 1：创建新的字典表和 BCNF 优化的表

执行 `05_schema_bcnf.sql` 文件：
```bash
mysql -u your_username -p drive_easy_pass < 05_schema_bcnf.sql
```

### 步骤 2：迁移现有数据（如果有）

```sql
-- 迁移线索来源数据
INSERT INTO enroll_leads_new (id, name, phone, source_id, intent_level_id, status_id, ...)
SELECT 
  el.id,
  el.name,
  el.phone,
  ls.id AS source_id,
  il.id AS intent_level_id,
  lst.id AS status_id,
  ...
FROM enroll_leads el
JOIN lead_sources ls ON ls.code = CASE 
  WHEN el.source = '线上渠道' THEN 'online'
  WHEN el.source = '线下渠道' THEN 'offline'
  -- ... 其他映射
END
JOIN intent_levels il ON il.code = CASE 
  WHEN el.intent_level = '高' THEN 'high'
  WHEN el.intent_level = '中' THEN 'medium'
  WHEN el.intent_level = '低' THEN 'low'
END
JOIN lead_statuses lst ON lst.code = CASE 
  WHEN el.status = '新线索' THEN 'new'
  WHEN el.status = '跟进中' THEN 'following'
  -- ... 其他映射
END;
```

### 步骤 3：验证数据完整性

```sql
-- 检查是否有未迁移的数据
SELECT COUNT(*) FROM enroll_leads WHERE source_id IS NULL;
SELECT COUNT(*) FROM enroll_leads WHERE intent_level_id IS NULL;
SELECT COUNT(*) FROM enroll_leads WHERE status_id IS NULL;
```

### 步骤 4：替换旧表

```sql
-- 备份旧表
RENAME TABLE enroll_leads TO enroll_leads_old,
             enroll_follow_ups TO enroll_follow_ups_old;

RENAME TABLE enroll_leads_new TO enroll_leads,
             enroll_follow_ups_new TO enroll_follow_ups;
```

---

## 七、注意事项

### 1. 应用层代码需要同步修改

需要修改以下地方的代码：
- 查询时需要进行 JOIN 操作获取字典值
- 插入/更新时需要使用字典表的 ID
- DTO 对象需要调整字段类型

**示例（Java）：**
```java
// 优化前
public record EnrollmentLeadDto(
    Long id,
    String name,
    String source,          // String 类型
    String intentLevel,     // String 类型
    String status,          // String 类型
    ...
) {}

// 优化后
public record EnrollmentLeadDto(
    Long id,
    String name,
    Long sourceId,          // 改为 Long 类型
    String sourceName,      // 新增：字典表的名称（通过 JOIN 获取）
    Long intentLevelId,     // 改为 Long 类型
    String intentLevelName, // 新增
    Long statusId,          // 改为 Long 类型
    String statusName,      // 新增
    ...
) {}
```

### 2. 查询语句需要调整

```sql
-- 优化前
SELECT id, name, source, intent_level, status FROM enroll_leads;

-- 优化后
SELECT 
  el.id, 
  el.name, 
  ls.name AS source_name,
  il.name AS intent_level_name,
  lst.name AS status_name
FROM enroll_leads el
JOIN lead_sources ls ON el.source_id = ls.id
JOIN intent_levels il ON el.intent_level_id = il.id
JOIN lead_statuses lst ON el.status_id = lst.id;
```

### 3. 性能考虑

- 增加了 JOIN 操作，可能影响查询性能
- 建议在字典表的外键字段上建立索引
- 字典表数据量小，JOIN 的性能开销可接受

---

## 八、总结

本次优化将数据库表结构从 3NF 提升到 BCNF，主要改进包括：

✅ 消除了传递依赖  
✅ 提取了 4 张字典表  
✅ 保证了数据一致性  
✅ 提高了系统的可维护性和扩展性  

虽然增加了一些表的数量，但这是符合数据库规范化理论的最佳实践，长期来看会大大降低维护成本。
