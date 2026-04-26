package org.dep.backend.controller;

import org.dep.backend.dto.ExamRouteDTO;
import org.dep.backend.dto.ExamScheduleDTO;
import org.dep.backend.dto.ExamVenueDTO;
import org.dep.backend.service.ExamVenueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamVenueController {

    private final ExamVenueService examVenueService;

    public ExamVenueController(ExamVenueService examVenueService) {
        this.examVenueService = examVenueService;
    }

    // ==================== 考场信息（公开） ====================

    /** 获取所有考场 */
    @GetMapping("/venues")
    public List<ExamVenueDTO> getAllVenues() {
        return examVenueService.getAllVenues();
    }

    /** 按区域查询考场 */
    @GetMapping("/venues/district/{district}")
    public List<ExamVenueDTO> getVenuesByDistrict(@PathVariable String district) {
        return examVenueService.getVenuesByDistrict(district);
    }

    /** 按考试类型查询考场 */
    @GetMapping("/venues/type/{examType}")
    public List<ExamVenueDTO> getVenuesByExamType(@PathVariable String examType) {
        return examVenueService.getVenuesByExamType(examType);
    }

    /** 获取考场详情 */
    @GetMapping("/venues/{venueId}")
    public ExamVenueDTO getVenueDetail(@PathVariable Long venueId) {
        return examVenueService.getVenueDetail(venueId);
    }

    /** 搜索考场 */
    @GetMapping("/venues/search")
    public List<ExamVenueDTO> searchVenues(@RequestParam String keyword) {
        return examVenueService.searchVenues(keyword);
    }

    // ==================== 考试线路图 ====================

    /** 获取考场线路 */
    @GetMapping("/routes/venue/{venueId}")
    public List<ExamRouteDTO> getVenueRoutes(@PathVariable Long venueId) {
        return examVenueService.getVenueRoutes(venueId);
    }

    /** 获取线路详情 */
    @GetMapping("/routes/{routeId}")
    public ExamRouteDTO getRouteDetail(@PathVariable Long routeId) {
        return examVenueService.getRouteDetail(routeId);
    }

    // ==================== 考试安排（报名用） ====================

    /**
     * 获取某考场的可用考试安排
     * 用于选择考场后，选择日期时间
     */
    @GetMapping("/venues/{venueId}/schedules")
    public List<ExamScheduleDTO> getVenueSchedules(
            @PathVariable Long venueId,
            @RequestParam(required = false) String examType
    ) {
        return examVenueService.getVenueAvailableSchedules(venueId, examType);
    }

    /**
     * 获取所有可预约的考试安排
     * 可按考试类型和区域筛选
     */
    @GetMapping("/schedules/available")
    public List<ExamScheduleDTO> getAvailableSchedules(
            @RequestParam(required = false) String examType,
            @RequestParam(required = false) String district
    ) {
        return examVenueService.getAvailableSchedules(examType, district);
    }
}