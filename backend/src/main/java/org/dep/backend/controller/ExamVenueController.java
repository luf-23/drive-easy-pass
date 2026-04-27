package org.dep.backend.controller;

import org.dep.backend.dto.*;
import org.dep.backend.service.ExamVenueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam")
public class ExamVenueController {

    private final ExamVenueService service;

    public ExamVenueController(ExamVenueService service) {
        this.service = service;
    }

    @GetMapping("/venues")
    public List<ExamVenueDTO> getAllVenues() { return service.getAllVenues(); }

    @GetMapping("/venues/district/{district}")
    public List<ExamVenueDTO> getByDistrict(@PathVariable String district) { return service.getVenuesByDistrict(district); }

    @GetMapping("/venues/type/{examType}")
    public List<ExamVenueDTO> getByType(@PathVariable String examType) { return service.getVenuesByExamType(examType); }

    @GetMapping("/venues/{venueId}")
    public ExamVenueDTO getDetail(@PathVariable Long venueId) { return service.getVenueDetail(venueId); }

    @GetMapping("/venues/search")
    public List<ExamVenueDTO> search(@RequestParam String keyword) { return service.searchVenues(keyword); }

    @GetMapping("/routes/venue/{venueId}")
    public List<ExamRouteDTO> getRoutes(@PathVariable Long venueId) { return service.getVenueRoutes(venueId); }

    @GetMapping("/routes/{routeId}")
    public ExamRouteDTO getRoute(@PathVariable Long routeId) { return service.getRouteDetail(routeId); }

    @GetMapping("/venues/{venueId}/schedules")
    public List<ExamScheduleDTO> getSchedules(@PathVariable Long venueId, @RequestParam(required = false) String examType) {
        return service.getVenueSchedules(venueId, examType);
    }

    @GetMapping("/schedules/available")
    public List<ExamScheduleDTO> getAvailable(@RequestParam(required = false) String examType,
                                              @RequestParam(required = false) String district) {
        return service.getAvailableSchedules(examType, district);
    }
}