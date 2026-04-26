package org.dep.backend.service;

import org.dep.backend.dto.ExamRegistrationDTO;
import org.dep.backend.dto.ExamRouteDTO;
import org.dep.backend.dto.ExamScheduleDTO;
import org.dep.backend.dto.ExamVenueDTO;
import org.dep.backend.model.ExamRouteDetail;
import org.dep.backend.model.ExamSchedule;
import org.dep.backend.model.ExamVenue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamVenueService {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ExamVenue> venueMapper = (rs, rowNum) -> new ExamVenue(
            rs.getLong("id"),
            rs.getString("venue_name"),
            rs.getString("venue_code"),
            rs.getString("address"),
            rs.getString("district"),
            rs.getString("contact_phone"),
            rs.getString("exam_type"),
            rs.getInt("total_slots"),
            rs.getInt("available_slots"),
            rs.getString("route_description"),
            rs.getString("route_map_url"),
            rs.getString("facilities"),
            rs.getString("business_hours"),
            rs.getBigDecimal("longitude"),
            rs.getBigDecimal("latitude"),
            rs.getString("status"),
            rs.getTimestamp("create_time").toLocalDateTime(),
            rs.getTimestamp("update_time").toLocalDateTime()
    );

    private final RowMapper<ExamRouteDetail> routeMapper = (rs, rowNum) -> new ExamRouteDetail(
            rs.getLong("id"),
            rs.getLong("venue_id"),
            rs.getString("route_name"),
            rs.getString("route_number"),
            rs.getString("description"),
            rs.getString("start_point"),
            rs.getString("end_point"),
            rs.getInt("distance"),
            rs.getString("difficulty"),
            rs.getString("points"),
            rs.getString("map_image_url"),
            rs.getInt("sort_order"),
            rs.getTimestamp("create_time").toLocalDateTime()
    );

    private final RowMapper<ExamSchedule> scheduleMapper = (rs, rowNum) -> new ExamSchedule(
            rs.getLong("id"),
            rs.getLong("venue_id"),
            rs.getDate("exam_date").toLocalDate(),
            rs.getTime("start_time").toLocalTime(),
            rs.getTime("end_time").toLocalTime(),
            rs.getString("exam_type"),
            rs.getInt("total_slots"),
            rs.getInt("reserved_slots"),
            rs.getInt("available_slots"),
            rs.getString("status"),
            rs.getTimestamp("create_time").toLocalDateTime()
    );

    public ExamVenueService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 获取所有考场 */
    public List<ExamVenueDTO> getAllVenues() {
        List<ExamVenue> venues = jdbcTemplate.query(
                "SELECT * FROM exam_venues WHERE status = 'ACTIVE' ORDER BY id",
                venueMapper
        );
        return venues.stream().map(this::toVenueDTO).toList();
    }

    /** 按区域查询考场 */
    public List<ExamVenueDTO> getVenuesByDistrict(String district) {
        List<ExamVenue> venues = jdbcTemplate.query(
                "SELECT * FROM exam_venues WHERE district = ? AND status = 'ACTIVE' ORDER BY id",
                venueMapper, district
        );
        return venues.stream().map(this::toVenueDTO).toList();
    }

    /** 按考试类型查询考场 */
    public List<ExamVenueDTO> getVenuesByExamType(String examType) {
        List<ExamVenue> venues = jdbcTemplate.query(
                "SELECT * FROM exam_venues WHERE exam_type = ? AND status = 'ACTIVE' ORDER BY id",
                venueMapper, examType
        );
        return venues.stream().map(this::toVenueDTO).toList();
    }

    /** 获取考场详情 */
    public ExamVenueDTO getVenueDetail(Long venueId) {
        List<ExamVenue> venues = jdbcTemplate.query(
                "SELECT * FROM exam_venues WHERE id = ?",
                venueMapper, venueId
        );
        if (venues.isEmpty()) {
            throw new IllegalArgumentException("考场不存在");
        }
        return toVenueDTO(venues.getFirst());
    }

    /** 搜索考场（按名称或地址） */
    public List<ExamVenueDTO> searchVenues(String keyword) {
        String likeKeyword = "%" + keyword + "%";
        List<ExamVenue> venues = jdbcTemplate.query(
                "SELECT * FROM exam_venues WHERE (venue_name LIKE ? OR address LIKE ?) AND status = 'ACTIVE'",
                venueMapper, likeKeyword, likeKeyword
        );
        return venues.stream().map(this::toVenueDTO).toList();
    }

    // ==================== 考试线路图 ====================

    /** 获取某考场的所有线路 */
    public List<ExamRouteDTO> getVenueRoutes(Long venueId) {
        List<ExamRouteDetail> routes = jdbcTemplate.query(
                "SELECT * FROM exam_route_details WHERE venue_id = ? ORDER BY sort_order",
                routeMapper, venueId
        );
        return routes.stream().map(this::toRouteDTO).toList();
    }

    /** 获取线路详情 */
    public ExamRouteDTO getRouteDetail(Long routeId) {
        List<ExamRouteDetail> routes = jdbcTemplate.query(
                "SELECT * FROM exam_route_details WHERE id = ?",
                routeMapper, routeId
        );
        if (routes.isEmpty()) {
            throw new IllegalArgumentException("线路不存在");
        }
        return toRouteDTO(routes.getFirst());
    }

    // ==================== 考试安排 ====================

    /** 获取某考场的所有可用考试安排（按日期分组） */
    public List<ExamScheduleDTO> getVenueAvailableSchedules(Long venueId, String examType) {
        String sql = """
                SELECT s.*, v.venue_name
                FROM exam_schedules s
                JOIN exam_venues v ON s.venue_id = v.id
                WHERE s.venue_id = ?
                AND s.status = 'OPEN'
                AND s.exam_date >= CURDATE()
                AND s.available_slots > 0
                """;

        if (examType != null && !examType.isBlank()) {
            sql += " AND s.exam_type = ?";
        }
        sql += " ORDER BY s.exam_date, s.start_time";

        if (examType != null && !examType.isBlank()) {
            return jdbcTemplate.query(sql, (rs, rowNum) -> toScheduleDTO(rs), venueId, examType);
        }
        return jdbcTemplate.query(sql, (rs, rowNum) -> toScheduleDTO(rs), venueId);
    }

    /** 获取所有可用考试安排 */
    public List<ExamScheduleDTO> getAvailableSchedules(String examType, String district) {
        String sql = """
                SELECT s.*, v.venue_name, v.district
                FROM exam_schedules s
                JOIN exam_venues v ON s.venue_id = v.id
                WHERE s.status = 'OPEN'
                AND s.exam_date >= CURDATE()
                AND s.available_slots > 0
                """;

        if (examType != null && !examType.isBlank()) {
            sql += " AND s.exam_type = '" + examType.replace("'", "''") + "'";
        }
        if (district != null && !district.isBlank()) {
            sql += " AND v.district = '" + district.replace("'", "''") + "'";
        }
        sql += " ORDER BY v.venue_name, s.exam_date, s.start_time LIMIT 100";

        return jdbcTemplate.query(sql, (rs, rowNum) -> toScheduleDTO(rs));
    }

    // ==================== 转换方法 ====================

    private ExamScheduleDTO toScheduleDTO(java.sql.ResultSet rs) throws java.sql.SQLException {
        String venueName = "";
        try {
            venueName = rs.getString("venue_name");
        } catch (Exception ignored) {}

        return new ExamScheduleDTO(
                rs.getLong("id"),
                rs.getLong("venue_id"),
                venueName,
                rs.getDate("exam_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(),
                rs.getString("exam_type"),
                rs.getInt("total_slots"),
                rs.getInt("reserved_slots"),
                rs.getInt("available_slots"),
                rs.getString("status")
        );
    }

    private ExamVenueDTO toVenueDTO(ExamVenue v) {
        return new ExamVenueDTO(
                v.id(), v.venueName(), v.venueCode(), v.address(),
                v.district(), v.contactPhone(), v.examType(),
                v.totalSlots(), v.availableSlots(),
                v.routeDescription(), v.routeMapUrl(),
                v.facilities(), v.businessHours(),
                v.longitude(), v.latitude(), v.status()
        );
    }

    private ExamRouteDTO toRouteDTO(ExamRouteDetail r) {
        return new ExamRouteDTO(
                r.id(), r.venueId(), r.routeName(), r.routeNumber(),
                r.description(), r.startPoint(), r.endPoint(),
                r.distance(), r.difficulty(), r.points(),
                r.mapImageUrl(), r.sortOrder()
        );
    }
}