package org.dep.backend.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.dep.backend.dto.EnrollmentDashboardDto;
import org.dep.backend.dto.EnrollmentFollowUpDto;
import org.dep.backend.dto.EnrollmentFollowUpRequest;
import org.dep.backend.dto.EnrollmentFunnelStatDto;
import org.dep.backend.dto.EnrollmentIntentStatDto;
import org.dep.backend.dto.EnrollmentLeadDto;
import org.dep.backend.dto.EnrollmentLeadRequest;
import org.dep.backend.dto.EnrollmentOwnerPerformanceDto;
import org.dep.backend.dto.EnrollmentSourceStatDto;
import org.dep.backend.dto.PageResult;
import org.dep.backend.dto.PublicEnrollmentIntentRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class EnrollmentService {
    private static final int MAX_PAGE_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<EnrollmentLeadDto> leadMapper = (rs, rowNum) -> new EnrollmentLeadDto(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getLong("source_id"),
            rs.getString("source_name"),
            rs.getLong("intent_level_id"),
            rs.getString("intent_level_name"),
            rs.getLong("status_id"),
            rs.getString("status_name"),
            rs.getObject("owner_user_id", Long.class),
            rs.getString("owner_name"),
            readDateTime(rs.getTimestamp("next_follow_time")),
            rs.getString("remark"),
            readDateTime(rs.getTimestamp("create_time")),
            readDateTime(rs.getTimestamp("update_time"))
    );

    private final RowMapper<EnrollmentFollowUpDto> followUpMapper = (rs, rowNum) -> new EnrollmentFollowUpDto(
            rs.getLong("id"),
            rs.getLong("lead_id"),
            rs.getString("content"),
            rs.getLong("follow_type_id"),
            rs.getString("follow_type_name"),
            readDateTime(rs.getTimestamp("next_follow_time")),
            rs.getObject("creator_user_id", Long.class),
            rs.getString("creator_name"),
            readDateTime(rs.getTimestamp("create_time"))
    );

    public EnrollmentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResult<EnrollmentLeadDto> queryLeads(String keyword,
                                                    String status,
                                                    String source,
                                                    Long ownerUserId,
                                                    String startDate,
                                                    String endDate,
                                                    Integer page,
                                                    Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, MAX_PAGE_SIZE);
        int offset = (safePage - 1) * safePageSize;

        List<String> conditions = new ArrayList<>();
        List<Object> args = new ArrayList<>();

        if (!normalize(keyword).isBlank()) {
            conditions.add("(l.name LIKE ? OR l.phone LIKE ?)");
            String pattern = "%" + normalize(keyword) + "%";
            args.add(pattern);
            args.add(pattern);
        }
        if (!normalize(status).isBlank()) {
            conditions.add("l.status_id = ?");
            args.add(getStatusIdByCode(normalize(status)));
        }
        if (!normalize(source).isBlank()) {
            conditions.add("l.source_id = ?");
            args.add(getSourceIdByCode(normalize(source)));
        }
        if (ownerUserId != null) {
            conditions.add("l.owner_user_id = ?");
            args.add(ownerUserId);
        }
        LocalDateTime startTime = parseDateTimeNullable(startDate);
        LocalDateTime endTime = parseDateTimeNullable(endDate);
        if (startTime != null) {
            conditions.add("l.create_time >= ?");
            args.add(toTimestamp(startTime));
        }
        if (endTime != null) {
            conditions.add("l.create_time <= ?");
            args.add(toTimestamp(endTime));
        }

        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM enroll_leads l" + whereClause,
                Long.class,
                args.toArray()
        );

        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add(safePageSize);
        listArgs.add(offset);

        List<EnrollmentLeadDto> items = jdbcTemplate.query("""
                SELECT l.id, l.name, l.phone,
                       l.source_id, ls.name AS source_name,
                       l.intent_level_id, il.name AS intent_level_name,
                       l.status_id, lst.name AS status_name,
                       l.owner_user_id, COALESCE(u.nickname, u.username) AS owner_name,
                       l.next_follow_time, l.remark, l.create_time, l.update_time
                FROM enroll_leads l
                LEFT JOIN users u ON u.id = l.owner_user_id
                LEFT JOIN lead_sources ls ON l.source_id = ls.id
                LEFT JOIN intent_levels il ON l.intent_level_id = il.id
                LEFT JOIN lead_statuses lst ON l.status_id = lst.id
                """ + whereClause + " ORDER BY l.create_time DESC, l.id DESC LIMIT ? OFFSET ?", leadMapper, listArgs.toArray());

        return new PageResult<>(items, total == null ? 0 : total);
    }

    public PageResult<EnrollmentLeadDto> listSignedStudents(String keyword, Integer page, Integer pageSize) {
        return queryLeads(keyword, "enrolled", null, null, null, null, page, pageSize);
    }

    public EnrollmentLeadDto createLead(EnrollmentLeadRequest request) {
        validateLeadRequest(request);
        
        // 提供默认值，防止NULL值导致数据库错误
        Long sourceId = request.sourceId() != null ? request.sourceId() : getSourceIdByCode("other");
        Long intentLevelId = request.intentLevelId() != null ? request.intentLevelId() : getIntentLevelIdByCode("medium");
        Long statusId = request.statusId() != null ? request.statusId() : getStatusIdByCode("new");

        jdbcTemplate.update("""
                INSERT INTO enroll_leads (name, phone, source_id, intent_level_id, status_id, owner_user_id, next_follow_time, remark)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                normalize(request.name()),
                normalize(request.phone()),
                sourceId,
                intentLevelId,
                statusId,
                request.ownerUserId(),
                toTimestamp(parseDateTimeNullable(request.nextFollowTime())),
                normalizeNullable(request.remark())
        );
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findLead(id);
    }

    public EnrollmentLeadDto updateLead(Long id, EnrollmentLeadRequest request) {
        validateLeadRequest(request);
        ensureLeadExists(id);
        
        // 提供默认值，防止NULL值导致数据库错误
        Long sourceId = request.sourceId() != null ? request.sourceId() : getSourceIdByCode("other");
        Long intentLevelId = request.intentLevelId() != null ? request.intentLevelId() : getIntentLevelIdByCode("medium");
        Long statusId = request.statusId() != null ? request.statusId() : getStatusIdByCode("new");

        jdbcTemplate.update("""
                UPDATE enroll_leads
                SET name = ?, phone = ?, source_id = ?, intent_level_id = ?, status_id = ?, owner_user_id = ?, next_follow_time = ?, remark = ?
                WHERE id = ?
                """,
                normalize(request.name()),
                normalize(request.phone()),
                sourceId,
                intentLevelId,
                statusId,
                request.ownerUserId(),
                toTimestamp(parseDateTimeNullable(request.nextFollowTime())),
                normalizeNullable(request.remark()),
                id
        );
        return findLead(id);
    }

        public EnrollmentLeadDto assignOwner(Long id, Long ownerUserId) {
        ensureLeadExists(id);
        jdbcTemplate.update("UPDATE enroll_leads SET owner_user_id = ? WHERE id = ?", ownerUserId, id);
        return findLead(id);
        }

        public EnrollmentLeadDto createLeadFromPublicIntent(PublicEnrollmentIntentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Intent request is required");
        }
        String name = normalize(request.name());
        String phone = normalize(request.phone());
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (phone.isBlank() || !phone.matches("^[0-9+\\-]{6,20}$")) {
            throw new IllegalArgumentException("Phone format is invalid");
        }

        String sourceCode = normalizeWithDefault(request.source(), "online");
        Long sourceId = getSourceIdByCode(sourceCode);
        Long intentLevelId = getIntentLevelIdByCode("medium");
        Long statusId = getStatusIdByCode("new");
        
        // 添加安全检查，防止字典表查询返回null
        if (sourceId == null) {
            sourceId = getSourceIdByCode("other");
        }
        if (intentLevelId == null) {
            intentLevelId = getIntentLevelIdByCode("medium");
        }
        if (statusId == null) {
            statusId = getStatusIdByCode("new");
        }

        String remark = normalizeWithDefault(request.remark(), "")
            + (normalize(request.vehicleType()).isBlank() ? "" : "；车型：" + normalize(request.vehicleType()))
            + (normalize(request.classType()).isBlank() ? "" : "；班型：" + normalize(request.classType()));

        jdbcTemplate.update("""
            INSERT INTO enroll_leads (name, phone, source_id, intent_level_id, status_id, owner_user_id, next_follow_time, remark)
            VALUES (?, ?, ?, ?, ?, NULL, NULL, ?)
            """,
            name,
            phone,
            sourceId,
            intentLevelId,
            statusId,
            remark
        );
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findLead(id);
        }

        public EnrollmentDashboardDto dashboard() {
        Long todayNewLeads = defaultZero(jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM enroll_leads
            WHERE DATE(create_time) = CURRENT_DATE
            """, Long.class));

        Long monthTotal = defaultZero(jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM enroll_leads
            WHERE DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURRENT_DATE, '%Y-%m')
            """, Long.class));

        Long monthSigned = defaultZero(jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM enroll_leads
            WHERE DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURRENT_DATE, '%Y-%m')
              AND status_id = ?
            """, Long.class, getStatusIdByCode("enrolled")));

        double monthConversionRate = monthTotal == 0 ? 0 : (monthSigned.doubleValue() / monthTotal.doubleValue()) * 100;

        List<EnrollmentSourceStatDto> sourceDistribution = jdbcTemplate.query("""
            SELECT ls.name, COUNT(*) AS cnt
            FROM enroll_leads el
            JOIN lead_sources ls ON el.source_id = ls.id
            GROUP BY ls.id, ls.name
            ORDER BY cnt DESC
            """, (rs, rowNum) -> new EnrollmentSourceStatDto(
            rs.getString("name"),
            rs.getLong("cnt")
        ));

        List<EnrollmentOwnerPerformanceDto> ownerRanking = jdbcTemplate.query("""
            SELECT l.owner_user_id,
                   COALESCE(u.nickname, u.username, '未分配') AS owner_name,
                   COUNT(*) AS cnt
            FROM enroll_leads l
            LEFT JOIN users u ON u.id = l.owner_user_id
            WHERE l.status_id = ?
            GROUP BY l.owner_user_id, owner_name
            ORDER BY cnt DESC
            LIMIT 10
            """, (rs, rowNum) -> new EnrollmentOwnerPerformanceDto(
            rs.getObject("owner_user_id", Long.class),
            rs.getString("owner_name"),
            rs.getLong("cnt")
        ), getStatusIdByCode("enrolled"));

        List<EnrollmentIntentStatDto> intentDistribution = jdbcTemplate.query("""
            SELECT il.name, COUNT(*) AS cnt
            FROM enroll_leads el
            JOIN intent_levels il ON el.intent_level_id = il.id
            GROUP BY il.id, il.name
            ORDER BY cnt DESC
            """, (rs, rowNum) -> new EnrollmentIntentStatDto(
            rs.getString("name"),
            rs.getLong("cnt")
        ));

        List<EnrollmentFunnelStatDto> funnel = List.of(
            new EnrollmentFunnelStatDto("线索", countByStatus(null)),
            new EnrollmentFunnelStatDto("到店", countByStatus("following")),
            new EnrollmentFunnelStatDto("报名", countByStatus("enrolled"))
        );

        return new EnrollmentDashboardDto(
            todayNewLeads,
            monthConversionRate,
            sourceDistribution,
            ownerRanking,
            intentDistribution,
            funnel
        );
        }

    public EnrollmentLeadDto findLead(Long id) {
        return jdbcTemplate.queryForObject("""
                SELECT l.id, l.name, l.phone,
                       l.source_id, ls.name AS source_name,
                       l.intent_level_id, il.name AS intent_level_name,
                       l.status_id, lst.name AS status_name,
                       l.owner_user_id, COALESCE(u.nickname, u.username) AS owner_name,
                       l.next_follow_time, l.remark, l.create_time, l.update_time
                FROM enroll_leads l
                LEFT JOIN users u ON u.id = l.owner_user_id
                LEFT JOIN lead_sources ls ON l.source_id = ls.id
                LEFT JOIN intent_levels il ON l.intent_level_id = il.id
                LEFT JOIN lead_statuses lst ON l.status_id = lst.id
                WHERE l.id = ?
                """, leadMapper, id);
    }

    public List<EnrollmentFollowUpDto> listFollowUps(Long leadId) {
        ensureLeadExists(leadId);
        return jdbcTemplate.query("""
                SELECT f.id, f.lead_id, f.content, f.follow_type_id, ft.name AS follow_type_name,
                       f.next_follow_time,
                       f.creator_user_id, COALESCE(u.nickname, u.username) AS creator_name, f.create_time
                FROM enroll_follow_ups f
                LEFT JOIN users u ON u.id = f.creator_user_id
                LEFT JOIN follow_types ft ON f.follow_type_id = ft.id
                WHERE f.lead_id = ?
                ORDER BY f.create_time DESC, f.id DESC
                """, followUpMapper, leadId);
    }

    public EnrollmentFollowUpDto addFollowUp(Long leadId, EnrollmentFollowUpRequest request, Long creatorUserId) {
        ensureLeadExists(leadId);
        validateFollowUpRequest(request);
        jdbcTemplate.update("""
                INSERT INTO enroll_follow_ups (lead_id, content, follow_type_id, next_follow_time, creator_user_id)
                VALUES (?, ?, ?, ?, ?)
                """,
                leadId,
                normalize(request.content()),
                request.followTypeId(),
                toTimestamp(parseDateTimeNullable(request.nextFollowTime())),
                creatorUserId
        );
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findFollowUp(id);
    }

    private EnrollmentFollowUpDto findFollowUp(Long id) {
        return jdbcTemplate.queryForObject("""
                SELECT f.id, f.lead_id, f.content, f.follow_type_id, ft.name AS follow_type_name,
                       f.next_follow_time,
                       f.creator_user_id, COALESCE(u.nickname, u.username) AS creator_name, f.create_time
                FROM enroll_follow_ups f
                LEFT JOIN users u ON u.id = f.creator_user_id
                LEFT JOIN follow_types ft ON f.follow_type_id = ft.id
                WHERE f.id = ?
                """, followUpMapper, id);
    }

    private void ensureLeadExists(Long leadId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM enroll_leads WHERE id = ?", Long.class, leadId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("Lead not found");
        }
    }

    private void validateLeadRequest(EnrollmentLeadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Lead request is required");
        }
        if (normalize(request.name()).isBlank()) {
            throw new IllegalArgumentException("Lead name is required");
        }
        String phone = normalize(request.phone());
        if (phone.isBlank()) {
            throw new IllegalArgumentException("Phone is required");
        }
        if (!phone.matches("^[0-9+\\-]{6,20}$")) {
            throw new IllegalArgumentException("Phone format is invalid");
        }
        parseDateTimeNullable(request.nextFollowTime());
    }

    private void validateFollowUpRequest(EnrollmentFollowUpRequest request) {
        if (request == null || normalize(request.content()).isBlank()) {
            throw new IllegalArgumentException("Follow up content is required");
        }
        parseDateTimeNullable(request.nextFollowTime());
    }

    private LocalDateTime parseDateTimeNullable(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Datetime format is invalid, expected yyyy-MM-ddTHH:mm");
        }
    }

    private Long countByStatus(String statusCode) {
        if (statusCode == null) {
            return defaultZero(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM enroll_leads", Long.class));
        }
        Long statusId = getStatusIdByCode(statusCode);
        if (statusId == null) {
            return 0L;
        }
        return defaultZero(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM enroll_leads WHERE status_id = ?", Long.class, statusId));
    }

    private Long defaultZero(Long value) {
        return value == null ? 0 : value;
    }

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private LocalDateTime readDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeWithDefault(String value, String defaultValue) {
        String normalized = normalize(value);
        return normalized.isBlank() ? defaultValue : normalized;
    }

    private String normalizeNullable(String value) {
        return value == null ? "" : value.trim();
    }

    // ========================================
    // BCNF 优化：字典表辅助方法
    // ========================================

    private Long getStatusIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        List<Long> results = jdbcTemplate.query(
            "SELECT id FROM lead_statuses WHERE code = ?", 
            (rs, rowNum) -> rs.getLong("id"),
            code
        );
        return results.isEmpty() ? null : results.getFirst();
    }

    private Long getSourceIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        List<Long> results = jdbcTemplate.query(
            "SELECT id FROM lead_sources WHERE code = ?", 
            (rs, rowNum) -> rs.getLong("id"),
            code
        );
        return results.isEmpty() ? null : results.getFirst();
    }

    private Long getIntentLevelIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        List<Long> results = jdbcTemplate.query(
            "SELECT id FROM intent_levels WHERE code = ?", 
            (rs, rowNum) -> rs.getLong("id"),
            code
        );
        return results.isEmpty() ? null : results.getFirst();
    }

    private Long getFollowTypeIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        List<Long> results = jdbcTemplate.query(
            "SELECT id FROM follow_types WHERE code = ?", 
            (rs, rowNum) -> rs.getLong("id"),
            code
        );
        return results.isEmpty() ? null : results.getFirst();
    }
}
