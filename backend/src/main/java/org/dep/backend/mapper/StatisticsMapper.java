package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    @Select("SELECT COUNT(*) FROM users")
    long countTotalUsers();

    @Select("SELECT COUNT(*) FROM enrollment_intents")
    long countTotalEnrollments();

    @Select("SELECT COUNT(*) FROM exam_registrations")
    long countTotalExams();

    @Select("SELECT DATE(create_time) as date, COUNT(*) as count " +
            "FROM enrollment_intents " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getEnrollmentTrend();

    @Select("SELECT " +
            "  SUM(CASE WHEN passed = 'Y' THEN 1 ELSE 0 END) as passedCount, " +
            "  COUNT(*) as totalCount " +
            "FROM exam_registrations " +
            "WHERE passed IS NOT NULL AND status = 'completed'")
    Map<String, Object> getPassRate(); 
}