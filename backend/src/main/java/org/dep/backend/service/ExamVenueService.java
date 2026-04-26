package org.dep.backend.service;

import org.dep.backend.dto.*;
import org.dep.backend.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.List;

@Service
public class ExamVenueService {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ExamVenue> venueMapper = (rs, rowNum) -> new ExamVenue(
            rs.getLong("id"), rs.getString("venue_name"), rs.getString("venue_code"),
            rs.getString("address"), rs.getString("district"), rs.getString("contact_phone"),
            rs.getString("exam_type"), rs.getInt("total_slots"), rs.getInt("available_slots"),
            rs.getString("route_description"), rs.getString("route_map_url"),
            rs.getString("facilities"), rs.getString("business_hours"),
            rs.getBigDecimal("longitude"), rs.getBigDecimal("latitude"),
            rs.getString("status"), rs.getTimestamp("create_time").toLocalDateTime(),
            rs.getTimestamp("update_time").toLocalDateTime()
    );

    private final RowMapper<ExamRouteDetail> routeMapper = (rs, rowNum) -> new ExamRouteDetail(
            rs.getLong("id"), rs.getLong("venue_id"), rs.getString("route_name"),
            rs.getString("route_number"), rs.getString("description"),
            rs.getString("start_point"), rs.getString("end_point"),
            rs.getInt("distance"), rs.getString("difficulty"), rs.getString("points"),
            rs.getString("map_image_url"), rs.getInt("sort_order"),
            rs.getTimestamp("create_time").toLocalDateTime()
    );

    public ExamVenueService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ExamVenueDTO> getAllVenues() {
        return jdbcTemplate.query("SELECT * FROM exam_venues WHERE status='ACTIVE' ORDER BY id", venueMapper)
                .stream().map(this::toDTO).toList();
    }

    public List<ExamVenueDTO> getVenuesByDistrict(String district) {
        return jdbcTemplate.query("SELECT * FROM exam_venues WHERE district=? AND status='ACTIVE'", venueMapper, district)
                .stream().map(this::toDTO).toList();
    }

    public List<ExamVenueDTO> getVenuesByExamType(String examType) {
        return jdbcTemplate.query("SELECT * FROM exam_venues WHERE exam_type=? AND status='ACTIVE'", venueMapper, examType)
                .stream().map(this::toDTO).toList();
    }

    public ExamVenueDTO getVenueDetail(Long venueId) {
        var list = jdbcTemplate.query("SELECT * FROM exam_venues WHERE id=?", venueMapper, venueId);
        if (list.isEmpty()) throw new IllegalArgumentException("考场不存在");
        return toDTO(list.getFirst());
    }

    public List<ExamVenueDTO> searchVenues(String keyword) {
        String kw = "%" + keyword + "%";
        return jdbcTemplate.query("SELECT * FROM exam_venues WHERE (venue_name LIKE ? OR address LIKE ?) AND status='ACTIVE'",
                venueMapper, kw, kw).stream().map(this::toDTO).toList();
    }

    public List<ExamRouteDTO> getVenueRoutes(Long venueId) {
        return jdbcTemplate.query("SELECT * FROM exam_route_details WHERE venue_id=? ORDER BY sort_order",
                routeMapper, venueId).stream().map(this::toRouteDTO).toList();
    }

    public ExamRouteDTO getRouteDetail(Long routeId) {
        var list = jdbcTemplate.query("SELECT * FROM exam_route_details WHERE id=?", routeMapper, routeId);
        if (list.isEmpty()) throw new IllegalArgumentException("线路不存在");
        return toRouteDTO(list.getFirst());
    }

    public List<ExamScheduleDTO> getVenueSchedules(Long venueId, String examType) {
        String sql = """
                SELECT s.*, v.venue_name FROM exam_schedules s
                JOIN exam_venues v ON s.venue_id=v.id
                WHERE s.venue_id=? AND s.status='OPEN' AND s.exam_date>=CURDATE()
                """;
        if (examType != null && !examType.isBlank()) sql += " AND s.exam_type='" + examType + "'";
        sql += " ORDER BY s.exam_date, s.start_time";
        return jdbcTemplate.query(sql, (rs, rowNum) -> toScheduleDTO(rs), venueId);
    }

    public List<ExamScheduleDTO> getAvailableSchedules(String examType, String district) {
        String sql = """
                SELECT s.*, v.venue_name FROM exam_schedules s
                JOIN exam_venues v ON s.venue_id=v.id
                WHERE s.status='OPEN' AND s.exam_date>=CURDATE()
                """;
        if (examType != null && !examType.isBlank()) sql += " AND s.exam_type='" + examType + "'";
        if (district != null && !district.isBlank()) sql += " AND v.district='" + district + "'";
        sql += " ORDER BY v.venue_name, s.exam_date, s.start_time LIMIT 100";
        return jdbcTemplate.query(sql, (rs, rowNum) -> toScheduleDTO(rs));
    }

    private ExamVenueDTO toDTO(ExamVenue v) {
        return new ExamVenueDTO(v.id(), v.venueName(), v.venueCode(), v.address(), v.district(),
                v.contactPhone(), v.examType(), v.totalSlots(), v.availableSlots(),
                v.routeDescription(), v.routeMapUrl(), v.facilities(), v.businessHours(),
                v.longitude(), v.latitude(), v.status());
    }

    private ExamRouteDTO toRouteDTO(ExamRouteDetail r) {
        return new ExamRouteDTO(r.id(), r.venueId(), r.routeName(), r.routeNumber(),
                r.description(), r.startPoint(), r.endPoint(), r.distance(),
                r.difficulty(), r.points(), r.mapImageUrl(), r.sortOrder());
    }

    private ExamScheduleDTO toScheduleDTO(ResultSet rs) throws java.sql.SQLException {
        String vn = "";
        try { vn = rs.getString("venue_name"); } catch (Exception ignored) {}
        return new ExamScheduleDTO(rs.getLong("id"), rs.getLong("venue_id"), vn,
                rs.getDate("exam_date").toLocalDate(), rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(), rs.getString("exam_type"),
                rs.getInt("total_slots"), rs.getInt("reserved_slots"),
                rs.getInt("total_slots") - rs.getInt("reserved_slots"), rs.getString("status"));
    }
}