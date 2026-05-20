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
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalUsers", statisticsMapper.countTotalUsers());
        overview.put("activeUsers", statisticsMapper.countActiveUsers());
        overview.put("totalEnrollments", statisticsMapper.countTotalEnrollments());
        overview.put("monthlyEnrollments", statisticsMapper.countMonthlyEnrollments());
        overview.put("totalExams", statisticsMapper.countTotalExams());
        return overview;
    }

    public List<Map<String, Object>> getEnrollmentTrend() {
        return statisticsMapper.getEnrollmentTrendByMonth();
    }

    public Map<String, Object> getPassRate() {
        Map<String, Object> rawData = statisticsMapper.getPassRate();
        Map<String, Object> result = new HashMap<>();
        long passedCount = 0;
        long totalCount = 0;
        if (rawData != null) {
            passedCount = numberValue(rawData, "passedCount");
            totalCount = numberValue(rawData, "totalCount");
        }
        double rate = totalCount > 0 ? (double) passedCount / totalCount : 0.0;
        result.put("passedCount", passedCount);
        result.put("totalCount", totalCount);
        result.put("rate", rate);
        appendSubjectPassRates(result, statisticsMapper.getPassRateByExamType());
        return result;
    }

    private void appendSubjectPassRates(Map<String, Object> result, List<Map<String, Object>> byExamType) {
        if (byExamType == null) {
            return;
        }
        for (Map<String, Object> row : byExamType) {
            String examType = stringValue(row.get("examType"));
            long passed = numberValue(row, "passedCount");
            long total = numberValue(row, "totalCount");
            int percentage = total > 0 ? (int) Math.round(passed * 100.0 / total) : 0;
            String key = subjectKey(examType);
            if (key != null) {
                result.put(key, percentage);
            }
        }
    }

    private String subjectKey(String examType) {
        if (examType == null) {
            return null;
        }
        return switch (examType.trim()) {
            case "科目一" -> "subject1";
            case "科目二" -> "subject2";
            case "科目三" -> "subject3";
            case "科目四" -> "subject4";
            default -> null;
        };
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private long numberValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
}
