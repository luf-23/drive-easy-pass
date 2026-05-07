package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dep.backend.dto.ExamRegistrationBrief;
import org.dep.backend.dto.ExamRegistrationDTO;
import org.dep.backend.dto.ExamReservationDTO;
import org.dep.backend.dto.ExamScheduleCard;
import org.dep.backend.dto.ExamScheduleSlot;

import java.util.List;

@Mapper
public interface ExamRegistrationMapper {

    @Select({
            "<script>",
            "SELECT es.id, es.venue_id AS venueId, ev.name AS venueName, es.exam_type AS examType,",
            "       es.exam_date AS examDate, es.start_time AS startTime, es.end_time AS endTime,",
            "       es.capacity AS capacity, IFNULL(oc.cnt, 0) AS bookedCount,",
            "       GREATEST(es.capacity - IFNULL(oc.cnt, 0), 0) AS availableSlots, es.remark AS remark",
            "FROM exam_schedules es",
            "JOIN exam_venues ev ON ev.id = es.venue_id",
            "LEFT JOIN (",
            "  SELECT schedule_id, COUNT(*) cnt FROM exam_registrations",
            "  WHERE status IN ('confirmed', 'completed', 'absent')",
            "  GROUP BY schedule_id",
            ") oc ON oc.schedule_id = es.id",
            "WHERE es.exam_date &gt;= CURDATE()",
            "<if test='examType != null and examType != \"\"'>",
            "  AND es.exam_type = #{examType}",
            "</if>",
            "ORDER BY es.exam_date, es.start_time, es.id",
            "</script>"
    })
    List<ExamScheduleCard> listScheduleCards(@Param("examType") String examType);

    @Select("""
            SELECT id,
                   venue_id AS venueId,
                   exam_type AS examType,
                   exam_date AS examDate,
                   start_time AS startTime,
                   end_time AS endTime,
                   capacity AS capacity,
                   remark AS remark
            FROM exam_schedules
            WHERE id = #{id}
            """)
    ExamScheduleSlot loadSchedule(@Param("id") Long id);

    @Select("""
            SELECT IFNULL(COUNT(*), 0)
            FROM exam_registrations
            WHERE schedule_id = #{scheduleId}
              AND status IN ('confirmed', 'completed', 'absent')
            """)
    int countActiveRegistrations(@Param("scheduleId") Long scheduleId);

    @Select("""
            SELECT COUNT(*)
            FROM exam_registrations er
            JOIN exam_schedules es ON es.id = er.schedule_id
            WHERE er.user_id = #{userId}
              AND es.exam_type = #{examType}
              AND er.status = 'confirmed'
            """)
    int countActiveSameSubject(@Param("userId") Long userId, @Param("examType") String examType);

    @Select("""
            SELECT COUNT(*)
            FROM exam_registrations er
            JOIN exam_schedules es ON es.id = er.schedule_id
            WHERE er.user_id = #{userId}
              AND es.exam_type = #{prereqType}
              AND er.status = 'completed'
              AND er.passed = 'Y'
            """)
    int countPassedSubject(@Param("userId") Long userId, @Param("prereqType") String prereqType);

    @Insert("""
            INSERT INTO exam_registrations (user_id, schedule_id, status, remark)
            VALUES (#{userId}, #{scheduleId}, 'confirmed', #{remark})
            """)
    int insertRegistration(@Param("userId") Long userId,
                           @Param("scheduleId") Long scheduleId,
                           @Param("remark") String remark);

    @Select("SELECT LAST_INSERT_ID()")
    Long lastInsertId();

    @Select("""
            SELECT er.id,
                   er.schedule_id AS scheduleId,
                   es.venue_id AS venueId,
                   ev.name AS venueName,
                   es.exam_type AS examType,
                   es.exam_date AS examDate,
                   es.start_time AS startTime,
                   es.end_time AS endTime,
                   er.status AS status,
                   er.score AS score,
                   er.passed AS passed,
                   GREATEST(es.capacity - IFNULL(oc.cnt, 0), 0) AS availableSlots
            FROM exam_registrations er
            JOIN exam_schedules es ON es.id = er.schedule_id
            JOIN exam_venues ev ON ev.id = es.venue_id
            LEFT JOIN (
                SELECT schedule_id, COUNT(*) cnt
                FROM exam_registrations
                WHERE status IN ('confirmed', 'completed', 'absent')
                GROUP BY schedule_id
            ) oc ON oc.schedule_id = es.id
            WHERE er.user_id = #{userId}
            ORDER BY es.exam_date DESC, es.start_time DESC, er.id DESC
            """)
    List<ExamRegistrationDTO> listMine(@Param("userId") Long userId);

    @Select("""
            SELECT er.id,
                   er.user_id AS userId,
                   u.username AS username,
                   er.schedule_id AS scheduleId,
                   es.venue_id AS venueId,
                   ev.name AS venueName,
                   es.exam_type AS examType,
                   es.exam_date AS examDate,
                   es.start_time AS startTime,
                   er.status AS status,
                   er.score AS score,
                   er.passed AS passed,
                   er.remark AS remark
            FROM exam_registrations er
            JOIN exam_schedules es ON es.id = er.schedule_id
            JOIN exam_venues ev ON ev.id = es.venue_id
            JOIN users u ON u.id = er.user_id
            ORDER BY es.exam_date DESC, es.start_time DESC, er.id DESC
            """)
    List<ExamReservationDTO> listAllForAdmin();

    @Select("""
            SELECT er.id,
                   er.user_id AS userId,
                   u.username AS username,
                   er.schedule_id AS scheduleId,
                   es.venue_id AS venueId,
                   ev.name AS venueName,
                   es.exam_type AS examType,
                   es.exam_date AS examDate,
                   es.start_time AS startTime,
                   er.status AS status,
                   er.score AS score,
                   er.passed AS passed,
                   er.remark AS remark
            FROM exam_registrations er
            JOIN exam_schedules es ON es.id = er.schedule_id
            JOIN exam_venues ev ON ev.id = es.venue_id
            JOIN users u ON u.id = er.user_id
            WHERE er.id = #{id}
            """)
    ExamReservationDTO findAdminRow(@Param("id") Long id);

    @Select("""
            SELECT er.user_id AS userId, er.status AS status,
                   es.exam_date AS examDate, es.start_time AS startTime
            FROM exam_registrations er
            JOIN exam_schedules es ON es.id = er.schedule_id
            WHERE er.id = #{id}
            """)
    ExamRegistrationBrief findBrief(@Param("id") Long id);

    @Update("""
            UPDATE exam_registrations er
            JOIN exam_schedules es ON es.id = er.schedule_id
            SET er.status = 'cancelled'
            WHERE er.id = #{id}
              AND er.user_id = #{userId}
              AND er.status = 'confirmed'
              AND (es.exam_date > CURDATE()
                   OR (es.exam_date = CURDATE() AND es.start_time > CURTIME()))
            """)
    int cancelConfirmedByOwner(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
            UPDATE exam_registrations
            SET status = #{status},
                score = #{score},
                passed = #{passed},
                remark = #{remark}
            WHERE id = #{id}
            """)
    int updateAdminFields(@Param("id") Long id,
                          @Param("status") String status,
                          @Param("score") Integer score,
                          @Param("passed") String passed,
                          @Param("remark") String remark);
}
