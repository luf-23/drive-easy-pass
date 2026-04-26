package org.dep.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.dep.backend.dto.ExamReservationDTO;
import org.dep.backend.dto.ExamScoreRequest;
import org.dep.backend.dto.ReservationRequest;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.service.ExamReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamReservationController {

    private final ExamReservationService reservationService;

    public ExamReservationController(ExamReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * 预约考试（报名）
     */
    @PostMapping("/reserve")
    public ExamReservationDTO reserve(
            @RequestBody ReservationRequest request,
            HttpServletRequest httpRequest
    ) {
        CurrentUser user = getCurrentUser(httpRequest);
        return reservationService.reserve(user.id(), request.scheduleId());
    }

    /**
     * 取消预约
     */
    @PostMapping("/cancel/{reservationId}")
    public ResponseEntity<String> cancel(
            @PathVariable Long reservationId,
            HttpServletRequest httpRequest
    ) {
        CurrentUser user = getCurrentUser(httpRequest);
        reservationService.cancelReservation(user.id(), reservationId);
        return ResponseEntity.ok("取消成功");
    }

    /**
     * 录入成绩（管理员功能）
     */
    @PostMapping("/score")
    public ExamReservationDTO recordScore(@RequestBody ExamScoreRequest request) {
        return reservationService.recordScore(request);
    }

    /**
     * 标记缺考
     */
    @PostMapping("/absent/{reservationId}")
    public ExamReservationDTO markAbsent(@PathVariable Long reservationId) {
        return reservationService.markAbsent(reservationId);
    }

    /**
     * 获取我的预约列表
     */
    @GetMapping("/my-reservations")
    public List<ExamReservationDTO> getMyReservations(
            @RequestParam(required = false) String status,
            HttpServletRequest httpRequest
    ) {
        CurrentUser user = getCurrentUser(httpRequest);
        return reservationService.getMyReservations(user.id(), status);
    }

    private CurrentUser getCurrentUser(HttpServletRequest request) {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        if (user == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return user;
    }
}