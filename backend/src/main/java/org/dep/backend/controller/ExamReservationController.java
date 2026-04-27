package org.dep.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.dep.backend.dto.*;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.service.ExamReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exam")
public class ExamReservationController {

    private final ExamReservationService service;

    public ExamReservationController(ExamReservationService service) {
        this.service = service;
    }

    @PostMapping("/reserve")
    public ExamReservationDTO reserve(@RequestBody ReservationRequest req, HttpServletRequest request) {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        if (user == null) throw new IllegalArgumentException("请先登录");
        return service.reserve(user.id(), req.scheduleId());
    }

    @PostMapping("/cancel/{reservationId}")
    public ResponseEntity<Map<String, String>> cancel(
            @PathVariable Long reservationId,
            HttpServletRequest request
    ) {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        if (user == null) throw new IllegalArgumentException("请先登录");
        service.cancel(user.id(), reservationId);
        return ResponseEntity.ok(Map.of("message", "取消成功"));
    }
    @PostMapping("/score")
    public ExamReservationDTO recordScore(@RequestBody ExamScoreRequest req) {
        return service.recordScore(req);
    }

    @GetMapping("/my-reservations")
    public List<ExamReservationDTO> myReservations(@RequestParam(required = false) String status,
                                                   HttpServletRequest request) {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        if (user == null) throw new IllegalArgumentException("请先登录");
        return service.getMyReservations(user.id(), status);
    }
}