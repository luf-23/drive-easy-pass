package org.dep.backend.controller;

import org.dep.backend.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        return statisticsService.getOverview();
    }

    @GetMapping("/enrollment-trend")
    public List<Map<String, Object>> getEnrollmentTrend() {
        return statisticsService.getEnrollmentTrend();
    }

    @GetMapping("/pass-rate")
    public Map<String, Object> getPassRate() {
        return statisticsService.getPassRate();
    }
}
