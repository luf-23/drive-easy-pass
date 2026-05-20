package org.dep.backend.service;

import org.dep.backend.mapper.StatisticsMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public StatisticsService(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    public Map<String, Object> getOverview() {
        long totalUsers = statisticsMapper.countTotalUsers();
        long totalEnrollments = statisticsMapper.countTotalEnrollments();
        long totalExams = statisticsMapper.countTotalExams();

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalUsers", totalUsers);
        overview.put("totalEnrollments", totalEnrollments);
        overview.put("totalExams", totalExams);
        return overview;
    }

    public List<Map<String, Object>> getEnrollmentTrend() {
        return statisticsMapper.getEnrollmentTrend();
    }

    public Map<String, Object> getPassRate() {
        Map<String, Object> rawData = statisticsMapper.getPassRate();
        Map<String, Object> result = new HashMap<>();
        if (rawData == null) {
            result.put("passedCount", 0);
            result.put("totalCount", 0);
            result.put("rate", 0.0);
            return result;
        }

        long passedCount = numberValue(rawData, "passedCount");
        long totalCount = numberValue(rawData, "totalCount");
        double rate = totalCount > 0 ? (double) passedCount / totalCount : 0.0;

        result.put("passedCount", passedCount);
        result.put("totalCount", totalCount);
        result.put("rate", rate);

        return result;
    }

    private long numberValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
}
