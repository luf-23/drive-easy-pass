package org.dep.backend.service;


import org.dep.backend.dto.*;
import org.dep.backend.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExamReservationService {

    private final JdbcTemplate jdbcTemplate;

    public ExamReservationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Transactional
    public ExamReservationDTO reserve(Long userId, Long scheduleId) {
        // 检查考试安排
        var list = jdbcTemplate.query("""
            SELECT s.*, v.venue_name FROM exam_schedules s
            JOIN exam_venues v ON s.venue_id=v.id WHERE s.id=?
            """, (rs, rowNum) -> new Object() {
            Long venueId = rs.getLong("venue_id");
            String examType = rs.getString("exam_type");
            LocalDate examDate = rs.getDate("exam_date").toLocalDate();
            java.time.LocalTime startTime = rs.getTime("start_time").toLocalTime();
            int totalSlots = rs.getInt("total_slots");
            int reservedSlots = rs.getInt("reserved_slots");
            int availableSlots = totalSlots - reservedSlots;
            String status = rs.getString("status");
            String venueName = rs.getString("venue_name");
        }, scheduleId);

        if (list.isEmpty()) throw new IllegalArgumentException("考试安排不存在");
        var s = list.getFirst();
        if (!"OPEN".equals(s.status)) throw new IllegalArgumentException("该场次已关闭");
        if (s.availableSlots <= 0) throw new IllegalArgumentException("该场次已满");
        if (s.examDate.isBefore(LocalDate.now())) throw new IllegalArgumentException("不能预约过去的考试");

        // 检查是否已预约同一场次
        int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exam_reservations WHERE user_id=? AND schedule_id=? AND status='RESERVED'",
                Integer.class, userId, scheduleId);
        if (count > 0) throw new IllegalArgumentException("已预约该场次");

        // 取消该用户同类型的所有旧预约
        List<Long> oldReservations = jdbcTemplate.queryForList(
                "SELECT id FROM exam_reservations WHERE user_id=? AND exam_type=? AND status='RESERVED'",
                Long.class, userId, s.examType);

        for (Long oldId : oldReservations) {
            Long oldScheduleId = jdbcTemplate.queryForObject(
                    "SELECT schedule_id FROM exam_reservations WHERE id=?", Long.class, oldId);
            jdbcTemplate.update("UPDATE exam_reservations SET status='CANCELLED' WHERE id=?", oldId);
            jdbcTemplate.update("UPDATE exam_schedules SET reserved_slots=reserved_slots-1 WHERE id=?", oldScheduleId);
        }
        jdbcTemplate.update("""
                INSERT INTO exam_reservations (user_id, schedule_id, venue_id, exam_type, exam_date, start_time, status)
                VALUES (?, ?, ?, ?, ?, ?, 'RESERVED')
                """, userId, scheduleId, s.venueId, s.examType, s.examDate, s.startTime);

        // 更新考试安排的已预约数
        jdbcTemplate.update("UPDATE exam_schedules SET reserved_slots=reserved_slots+1 WHERE id=?", scheduleId);

        // 同步更新考场的可预约数
        jdbcTemplate.update("""
                UPDATE exam_venues v
                SET v.available_slots = (
                    SELECT COALESCE(SUM(s.total_slots - s.reserved_slots), 0)
                    FROM exam_schedules s
                    WHERE s.venue_id = v.id AND s.status = 'OPEN'
                )
                WHERE v.id = ?
        """, s.venueId);

        Long newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return getDetail(userId, newId);
    }

    @Transactional
    public void cancel(Long userId, Long reservationId) {
        var r = findById(reservationId);
        if (!r.userId().equals(userId)) throw new IllegalArgumentException("无权操作");
        if (!"RESERVED".equals(r.status())) throw new IllegalArgumentException("状态不允许取消");

        jdbcTemplate.update("UPDATE exam_reservations SET status='CANCELLED' WHERE id=? AND user_id=?", reservationId, userId);
        jdbcTemplate.update("UPDATE exam_schedules SET reserved_slots=reserved_slots-1 WHERE id=?", r.scheduleId());

        // 同步考场的可预约数
        jdbcTemplate.update("""
        UPDATE exam_venues v
        SET v.available_slots = (
            SELECT COALESCE(SUM(s.total_slots - s.reserved_slots), 0)
            FROM exam_schedules s
            WHERE s.venue_id = v.id AND s.status = 'OPEN'
        )
        WHERE v.id = ?
    """, r.venueId());
    }

    public ExamReservationDTO recordScore(ExamScoreRequest req) {
        var r = findById(req.reservationId());
        String passed = req.score() >= 90 ? "Y" : "N";
        jdbcTemplate.update("UPDATE exam_reservations SET status='COMPLETED', score=?, passed=? WHERE id=?",
                req.score(), passed, req.reservationId());
        return getDetail(r.userId(), req.reservationId());
    }

    public List<ExamReservationDTO> getMyReservations(Long userId, String status) {
        String sql = """
                SELECT r.*, u.username, v.venue_name FROM exam_reservations r
                JOIN users u ON r.user_id=u.id JOIN exam_venues v ON r.venue_id=v.id
                WHERE r.user_id=?
                """;
        if (status != null && !status.isBlank()) sql += " AND r.status='" + status + "'";
        sql += " ORDER BY r.exam_date DESC, r.create_time DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> toDTO(rs), userId);
    }

    private ExamReservation findById(Long id) {
        var list = jdbcTemplate.query("SELECT * FROM exam_reservations WHERE id=?",
                (rs, rowNum) -> new ExamReservation(
                        rs.getLong("id"), rs.getLong("user_id"), rs.getLong("schedule_id"),
                        rs.getLong("venue_id"), rs.getString("exam_type"),
                        rs.getDate("exam_date").toLocalDate(),
                        rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null,
                        rs.getString("status"), rs.getInt("score") != 0 ? rs.getInt("score") : null,
                        rs.getString("passed"), rs.getString("remark"),
                        rs.getTimestamp("create_time").toLocalDateTime(),
                        rs.getTimestamp("update_time").toLocalDateTime()
                ), id);
        if (list.isEmpty()) throw new IllegalArgumentException("预约不存在");
        return list.getFirst();
    }

    private ExamReservationDTO getDetail(Long userId, Long reservationId) {
        var list = jdbcTemplate.query("""
                SELECT r.*, u.username, v.venue_name FROM exam_reservations r
                JOIN users u ON r.user_id=u.id JOIN exam_venues v ON r.venue_id=v.id
                WHERE r.id=? AND r.user_id=?
                """, (rs, rowNum) -> toDTO(rs), reservationId, userId);
        if (list.isEmpty()) throw new IllegalArgumentException("预约不存在");
        return list.getFirst();
    }

    private ExamReservationDTO toDTO(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ExamReservationDTO(
                rs.getLong("id"), rs.getLong("user_id"), rs.getString("username"),
                rs.getLong("schedule_id"), rs.getLong("venue_id"), rs.getString("venue_name"),
                rs.getString("exam_type"), rs.getDate("exam_date").toLocalDate(),
                rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null,
                rs.getString("status"), rs.getInt("score") != 0 ? rs.getInt("score") : null,
                rs.getString("passed"), rs.getString("remark"));
    }
}