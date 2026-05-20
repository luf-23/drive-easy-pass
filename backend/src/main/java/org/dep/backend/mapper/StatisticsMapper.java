package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    @Select("SELECT COUNT(*) FROM users")
    long countTotalUsers();

    @Select("SELECT COUNT(*) FROM users WHERE status = 1")
    long countActiveUsers();

    @Select("SELECT COUNT(*) FROM enrollment_intents")
    long countTotalEnrollments();

    @Select("SELECT COUNT(*) FROM enrollment_intents " +
            "WHERE YEAR(create_time) = YEAR(CURDATE()) AND MONTH(create_time) = MONTH(CURDATE())")
    long countMonthlyEnrollments();

    @Select("SELECT COUNT(*) FROM exam_registrations")
    long countTotalExams();

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, COUNT(*) AS count " +
            "FROM enrollment_intents " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY month ASC")
    List<Map<String, Object>> getEnrollmentTrendByMonth();

    @Select("SELECT " +
            "  COALESCE(SUM(CASE WHEN passed = 'Y' THEN 1 ELSE 0 END), 0) AS passedCount, " +
            "  COUNT(*) AS totalCount " +
            "FROM exam_registrations " +
            "WHERE passed IS NOT NULL AND status = 'completed'")
    Map<String, Object> getPassRate();

    @Select("SELECT es.exam_type AS examType, " +
            "  COALESCE(SUM(CASE WHEN er.passed = 'Y' THEN 1 ELSE 0 END), 0) AS passedCount, " +
            "  COUNT(*) AS totalCount " +
            "FROM exam_registrations er " +
            "INNER JOIN exam_schedules es ON es.id = er.schedule_id " +
            "WHERE er.passed IS NOT NULL AND er.status = 'completed' " +
            "GROUP BY es.exam_type")
    List<Map<String, Object>> getPassRateByExamType();
}
