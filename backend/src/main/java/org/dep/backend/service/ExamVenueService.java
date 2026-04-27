package org.dep.backend.service;

import org.dep.backend.dto.*;
import org.dep.backend.mapper.ExamVenueMapper;
import org.dep.backend.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamVenueService {

    private final ExamVenueMapper examVenueMapper;

    public ExamVenueService(ExamVenueMapper examVenueMapper) {
        this.examVenueMapper = examVenueMapper;
    }

    public List<ExamVenueDTO> getAllVenues() {
        return examVenueMapper.listActiveVenues()
                .stream().map(this::toDTO).toList();
    }

    public List<ExamVenueDTO> getVenuesByDistrict(String district) {
        return examVenueMapper.listVenuesByDistrict(district)
                .stream().map(this::toDTO).toList();
    }

    public List<ExamVenueDTO> getVenuesByExamType(String examType) {
        return examVenueMapper.listVenuesByExamType(examType)
                .stream().map(this::toDTO).toList();
    }

    public ExamVenueDTO getVenueDetail(Long venueId) {
        ExamVenue venue = examVenueMapper.findVenueById(venueId);
        if (venue == null) throw new IllegalArgumentException("考场不存在");
        return toDTO(venue);
    }

    public List<ExamVenueDTO> searchVenues(String keyword) {
        return examVenueMapper.searchVenues(keyword).stream().map(this::toDTO).toList();
    }

    public List<ExamRouteDTO> getVenueRoutes(Long venueId) {
        return examVenueMapper.listVenueRoutes(venueId).stream().map(this::toRouteDTO).toList();
    }

    public ExamRouteDTO getRouteDetail(Long routeId) {
        ExamRouteDetail routeDetail = examVenueMapper.findRouteById(routeId);
        if (routeDetail == null) throw new IllegalArgumentException("线路不存在");
        return toRouteDTO(routeDetail);
    }

    public List<ExamScheduleDTO> getVenueSchedules(Long venueId, String examType) {
        return examVenueMapper.listVenueSchedules(venueId, examType);
    }

    public List<ExamScheduleDTO> getAvailableSchedules(String examType, String district) {
        return examVenueMapper.listAvailableSchedules(examType, district);
    }

    private ExamVenueDTO toDTO(ExamVenue v) {
        return new ExamVenueDTO(v.id(), v.venueName(), v.venueCode(), v.address(), v.district(),
                v.contactPhone(), v.examType(), v.totalSlots(), v.availableSlots(),
                v.routeDescription(), v.routeMapUrl(), v.facilities(), v.businessHours(),
                v.longitude(), v.latitude(), v.status());
    }

    private ExamRouteDTO toRouteDTO(ExamRouteDetail r) {
        return new ExamRouteDTO(r.id(), r.venueId(), r.routeName(), r.routeNumber(),
                r.description(), r.startPoint(), r.endPoint(), r.distance(),
                r.difficulty(), r.points(), r.mapImageUrl(), r.sortOrder());
    }
}