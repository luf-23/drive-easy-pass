package org.dep.backend.service;


import org.dep.backend.dto.ExamReservationDTO;
import org.dep.backend.dto.ExamScoreRequest;
import org.dep.backend.model.ExamReservation;
import org.dep.backend.model.ExamSchedule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExamReservationService {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ExamReservation> reservationMapper = (rs, rowNum) -> new ExamReservation(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getLong("schedule_id"),
            rs.getLong("venue_id"),
            rs.getString("exam_type"),
            rs.getDate("exam_date").toLocalDate(),
            rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null,
            rs.getString("status"),
            rs.getInt("score") != 0 ? rs.getInt("score") : null,
            rs.getString("passed"),
            rs.getString("remark"),
            rs.getTimestamp("create_time").toLocalDateTime(),
            rs.getTimestamp("update_time").toLocalDateTime()
    );

    public ExamReservationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 预约考试（报名）
     */
    @Transactional
    public ExamReservationDTO reserve(Long userId, Long scheduleId) {
        // 检查考试安排是否存在且可预约
        List<ExamSchedule> schedules = jdbcTemplate.query(
                "SELECT s.*, v.venue_name FROM exam_schedules s JOIN exam_venues v ON s.venue_id = v.id WHERE s.id = ?",
                (rs, rowNum) -> new ExamSchedule(
                        rs.getLong("id"), rs.getLong("venue_id"),
                        rs.getDate("exam_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getString("exam_type"),
                        rs.getInt("total_slots"), rs.getInt("reserved_slots"),
                        rs.getInt("available_slots"), rs.getString("status"),
                        rs.getTimestamp("create_time").toLocalDateTime()
                ), scheduleId
        );

        if (schedules.isEmpty()) {
            throw new IllegalArgumentException("考试安排不存在");
        }

        ExamSchedule schedule = schedules.getFirst();
        if (!"OPEN".equals(schedule.status())) {
            throw new IllegalArgumentException("该场次已关闭");
        }
        if (schedule.availableSlots() <= 0) {
            throw new IllegalArgumentException("该场次已满");
        }
        if (schedule.examDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("不能预约过去的考试");
        }

        // 检查是否已预约同一场次
        int exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exam_reservations WHERE user_id = ? AND schedule_id = ? AND status != 'CANCELLED'",
                Integer.class, userId, scheduleId
        );
        if (exists > 0) {
            throw new IllegalArgumentException("已预约该场次");
        }

        // 检查是否已有同类型未完成的预约
        int sameType = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exam_reservations WHERE user_id = ? AND exam_type = ? AND status IN ('RESERVED', 'COMPLETED')",
                Integer.class, userId, schedule.examType()
        );
        if (sameType > 0) {
            throw new IllegalArgumentException("已有" + schedule.examType() + "的预约，请先完成或取消");
        }

        // 插入预约记录
        jdbcTemplate.update("""
                INSERT INTO exam_reservations (user_id, schedule_id, venue_id, exam_type, exam_date, start_time, status)
                VALUES (?, ?, ?, ?, ?, ?, 'RESERVED')
                """, userId, scheduleId, schedule.venueId(), schedule.examType(),
                schedule.examDate(), schedule.startTime()
        );

        // 更新考试安排的已预约人数
        jdbcTemplate.update(
                "UPDATE exam_schedules SET reserved_slots = reserved_slots + 1 WHERE id = ?",
                scheduleId
        );

        // 查询刚插入的预约
        return getReservationDetail(userId, jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()", Long.class
        ));
    }

    /**
     * 取消预约
     */
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        ExamReservation reservation = findReservationById(reservationId);
        if (!reservation.userId().equals(userId)) {
            throw new IllegalArgumentException("无权取消他人的预约");
        }
        if (!"RESERVED".equals(reservation.status())) {
            throw new IllegalArgumentException("只能取消已预约的考试");
        }
        if (reservation.examDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("不能取消已过期的考试");
        }

        jdbcTemplate.update(
                "UPDATE exam_reservations SET status = 'CANCELLED' WHERE id = ? AND user_id = ?",
                reservationId, userId
        );

        jdbcTemplate.update(
                "UPDATE exam_schedules SET reserved_slots = reserved_slots - 1 WHERE id = ?",
                reservation.scheduleId()
        );
    }

    /**
     * 录入考试成绩
     */
    public ExamReservationDTO recordScore(ExamScoreRequest request) {
        ExamReservation reservation = findReservationById(request.reservationId());
        if (!"RESERVED".equals(reservation.status())) {
            throw new IllegalArgumentException("该预约状态为" + reservation.status() + "，无法录入成绩");
        }

        int score = request.score();
        String passed = score >= 90 ? "Y" : "N";

        jdbcTemplate.update(
                "UPDATE exam_reservations SET status = 'COMPLETED', score = ?, passed = ? WHERE id = ?",
                score, passed, request.reservationId()
        );

        return getReservationDetail(reservation.userId(), request.reservationId());
    }

    /**
     * 标记缺考
     */
    public ExamReservationDTO markAbsent(Long reservationId) {
        jdbcTemplate.update(
                "UPDATE exam_reservations SET status = 'ABSENT', score = 0, passed = 'N' WHERE id = ?",
                reservationId
        );
        ExamReservation reservation = findReservationById(reservationId);
        return getReservationDetail(reservation.userId(), reservationId);
    }

    /**
     * 获取我的预约列表
     */
    public List<ExamReservationDTO> getMyReservations(Long userId, String status) {
        String sql = """
                SELECT r.*, u.username, v.venue_name
                FROM exam_reservations r
                JOIN users u ON r.user_id = u.id
                JOIN exam_venues v ON r.venue_id = v.id
                WHERE r.user_id = ?
                """;
        if (status != null && !status.isBlank()) {
            sql += " AND r.status = '" + status + "'";
        }
        sql += " ORDER BY r.exam_date DESC, r.create_time DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExamReservationDTO(
                rs.getLong("id"), rs.getLong("user_id"), rs.getString("username"),
                rs.getLong("schedule_id"), rs.getLong("venue_id"), rs.getString("venue_name"),
                rs.getString("exam_type"), rs.getDate("exam_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(), rs.getString("status"),
                rs.getInt("score") != 0 ? rs.getInt("score") : null,
                rs.getString("passed"), rs.getString("remark")
        ), userId);
    }

    /**
     * 获取预约详情
     */
    private ExamReservationDTO getReservationDetail(Long userId, Long reservationId) {
        List<ExamReservationDTO> list = jdbcTemplate.query("""
                SELECT r.*, u.username, v.venue_name
                FROM exam_reservations r
                JOIN users u ON r.user_id = u.id
                JOIN exam_venues v ON r.venue_id = v.id
                WHERE r.id = ? AND r.user_id = ?
                """, (rs, rowNum) -> new ExamReservationDTO(
                rs.getLong("id"), rs.getLong("user_id"), rs.getString("username"),
                rs.getLong("schedule_id"), rs.getLong("venue_id"), rs.getString("venue_name"),
                rs.getString("exam_type"), rs.getDate("exam_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(), rs.getString("status"),
                rs.getInt("score") != 0 ? rs.getInt("score") : null,
                rs.getString("passed"), rs.getString("remark")
        ), reservationId, userId);

        if (list.isEmpty()) {
            throw new IllegalArgumentException("预约不存在");
        }
        return list.getFirst();
    }

    /**
     * 根据ID查找预约
     */
    private ExamReservation findReservationById(Long id) {
        List<ExamReservation> list = jdbcTemplate.query(
                "SELECT * FROM exam_reservations WHERE id = ?",
                reservationMapper, id
        );
        if (list.isEmpty()) {
            throw new IllegalArgumentException("预约不存在");
        }
        return list.getFirst();
    }
}