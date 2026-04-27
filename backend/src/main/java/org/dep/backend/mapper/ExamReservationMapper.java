package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dep.backend.dto.ExamReservationDTO;
import org.dep.backend.mapper.projection.ReservationBrief;
import org.dep.backend.mapper.projection.ReservationScheduleInfo;
import org.dep.backend.model.ExamReservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Mapper
public interface ExamReservationMapper {

    @Select("""
            SELECT s.venue_id AS venueId,
                   s.exam_type AS examType,
                   s.exam_date AS examDate,
                   s.start_time AS startTime,
                   (s.total_slots - s.reserved_slots) AS availableSlots,
                   s.status,
                   v.venue_name AS venueName
            FROM exam_schedules s
            JOIN exam_venues v ON s.venue_id = v.id
            WHERE s.id = #{scheduleId}
            """)
    ReservationScheduleInfo findScheduleForReservation(@Param("scheduleId") Long scheduleId);

    @Select("""
            SELECT COUNT(*)
            FROM exam_reservations
            WHERE user_id = #{userId}
              AND schedule_id = #{scheduleId}
              AND status = 'RESERVED'
            """)
    Integer countReservedByUserAndSchedule(@Param("userId") Long userId,
                                           @Param("scheduleId") Long scheduleId);

    @Select("""
            SELECT id,
                   schedule_id AS scheduleId
            FROM exam_reservations
            WHERE user_id = #{userId}
              AND exam_type = #{examType}
              AND status = 'RESERVED'
            """)
    List<ReservationBrief> listReservedByUserAndExamType(@Param("userId") Long userId,
                                                         @Param("examType") String examType);

    @Update("UPDATE exam_reservations SET status = #{status} WHERE id = #{id}")
    int updateReservationStatus(@Param("id") Long id, @Param("status") String status);

    @Insert("""
            INSERT INTO exam_reservations (user_id, schedule_id, venue_id, exam_type, exam_date, start_time, status)
            VALUES (#{userId}, #{scheduleId}, #{venueId}, #{examType}, #{examDate}, #{startTime}, #{status})
            """)
    int insertReservation(@Param("userId") Long userId,
                          @Param("scheduleId") Long scheduleId,
                          @Param("venueId") Long venueId,
                          @Param("examType") String examType,
                          @Param("examDate") LocalDate examDate,
                          @Param("startTime") LocalTime startTime,
                          @Param("status") String status);

    @Update("UPDATE exam_schedules SET reserved_slots = reserved_slots + 1 WHERE id = #{scheduleId}")
    int increaseReservedSlots(@Param("scheduleId") Long scheduleId);

    @Update("UPDATE exam_schedules SET reserved_slots = reserved_slots - 1 WHERE id = #{scheduleId}")
    int decreaseReservedSlots(@Param("scheduleId") Long scheduleId);

    @Update("""
            UPDATE exam_venues v
            SET v.available_slots = (
                SELECT COALESCE(SUM(s.total_slots - s.reserved_slots), 0)
                FROM exam_schedules s
                WHERE s.venue_id = v.id
                  AND s.status = 'OPEN'
            )
            WHERE v.id = #{venueId}
            """)
    int refreshVenueAvailableSlots(@Param("venueId") Long venueId);

    @Select("SELECT LAST_INSERT_ID()")
    Long lastInsertId();

    @Select("""
            SELECT id,
                   user_id AS userId,
                   schedule_id AS scheduleId,
                   venue_id AS venueId,
                   exam_type AS examType,
                   exam_date AS examDate,
                   start_time AS startTime,
                   status,
                   score,
                   passed,
                   remark,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM exam_reservations
            WHERE id = #{id}
            """)
    ExamReservation findReservationById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT r.id,",
            "       r.user_id AS userId,",
            "       u.username,",
            "       r.schedule_id AS scheduleId,",
            "       r.venue_id AS venueId,",
            "       v.venue_name AS venueName,",
            "       r.exam_type AS examType,",
            "       r.exam_date AS examDate,",
            "       r.start_time AS startTime,",
            "       r.status,",
            "       r.score,",
            "       r.passed,",
            "       r.remark",
            "FROM exam_reservations r",
            "JOIN users u ON r.user_id = u.id",
            "JOIN exam_venues v ON r.venue_id = v.id",
            "WHERE r.user_id = #{userId}",
            "<if test='status != null and status != \"\"'>",
            "  AND r.status = #{status}",
            "</if>",
            "ORDER BY r.exam_date DESC, r.create_time DESC",
            "</script>"
    })
    List<ExamReservationDTO> listMyReservations(@Param("userId") Long userId,
                                                @Param("status") String status);

    @Select("""
            SELECT r.id,
                   r.user_id AS userId,
                   u.username,
                   r.schedule_id AS scheduleId,
                   r.venue_id AS venueId,
                   v.venue_name AS venueName,
                   r.exam_type AS examType,
                   r.exam_date AS examDate,
                   r.start_time AS startTime,
                   r.status,
                   r.score,
                   r.passed,
                   r.remark
            FROM exam_reservations r
            JOIN users u ON r.user_id = u.id
            JOIN exam_venues v ON r.venue_id = v.id
            WHERE r.id = #{reservationId}
              AND r.user_id = #{userId}
            """)
    ExamReservationDTO findReservationDetail(@Param("userId") Long userId,
                                             @Param("reservationId") Long reservationId);

    @Update("""
            UPDATE exam_reservations
            SET status = 'CANCELLED'
            WHERE id = #{reservationId}
              AND user_id = #{userId}
            """)
    int cancelReservationByUser(@Param("userId") Long userId,
                                @Param("reservationId") Long reservationId);

    @Update("""
            UPDATE exam_reservations
            SET status = 'COMPLETED',
                score = #{score},
                passed = #{passed}
            WHERE id = #{reservationId}
            """)
    int completeReservation(@Param("reservationId") Long reservationId,
                            @Param("score") Integer score,
                            @Param("passed") String passed);
}
