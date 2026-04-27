package org.dep.backend.service;


import org.dep.backend.dto.*;
import org.dep.backend.mapper.ExamReservationMapper;
import org.dep.backend.mapper.projection.ReservationBrief;
import org.dep.backend.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExamReservationService {

    private final ExamReservationMapper examReservationMapper;

    public ExamReservationService(ExamReservationMapper examReservationMapper) {
        this.examReservationMapper = examReservationMapper;
    }


    @Transactional
    public ExamReservationDTO reserve(Long userId, Long scheduleId) {
        var s = examReservationMapper.findScheduleForReservation(scheduleId);
        if (s == null) throw new IllegalArgumentException("考试安排不存在");
        if (!"OPEN".equals(s.status())) throw new IllegalArgumentException("该场次已关闭");
        if (s.availableSlots() <= 0) throw new IllegalArgumentException("该场次已满");
        if (s.examDate().isBefore(LocalDate.now())) throw new IllegalArgumentException("不能预约过去的考试");

        // 检查是否已预约同一场次
        Integer count = examReservationMapper.countReservedByUserAndSchedule(userId, scheduleId);
        if (count > 0) throw new IllegalArgumentException("已预约该场次");

        // 取消该用户同类型的所有旧预约
        List<ReservationBrief> oldReservations = examReservationMapper.listReservedByUserAndExamType(userId, s.examType());

        for (ReservationBrief oldReservation : oldReservations) {
            examReservationMapper.updateReservationStatus(oldReservation.id(), "CANCELLED");
            examReservationMapper.decreaseReservedSlots(oldReservation.scheduleId());
        }
        examReservationMapper.insertReservation(userId, scheduleId, s.venueId(), s.examType(), s.examDate(), s.startTime(), "RESERVED");

        // 更新考试安排的已预约数
        examReservationMapper.increaseReservedSlots(scheduleId);

        // 同步更新考场的可预约数
        examReservationMapper.refreshVenueAvailableSlots(s.venueId());

        Long newId = examReservationMapper.lastInsertId();
        return getDetail(userId, newId);
    }

    @Transactional
    public void cancel(Long userId, Long reservationId) {
        var r = findById(reservationId);
        if (!r.userId().equals(userId)) throw new IllegalArgumentException("无权操作");
        if (!"RESERVED".equals(r.status())) throw new IllegalArgumentException("状态不允许取消");

        examReservationMapper.cancelReservationByUser(userId, reservationId);
        examReservationMapper.decreaseReservedSlots(r.scheduleId());

        // 同步考场的可预约数
        examReservationMapper.refreshVenueAvailableSlots(r.venueId());
    }

    public ExamReservationDTO recordScore(ExamScoreRequest req) {
        var r = findById(req.reservationId());
        String passed = req.score() >= 90 ? "Y" : "N";
        examReservationMapper.completeReservation(req.reservationId(), req.score(), passed);
        return getDetail(r.userId(), req.reservationId());
    }

    public List<ExamReservationDTO> getMyReservations(Long userId, String status) {
        return examReservationMapper.listMyReservations(userId, status);
    }

    private ExamReservation findById(Long id) {
        ExamReservation reservation = examReservationMapper.findReservationById(id);
        if (reservation == null) throw new IllegalArgumentException("预约不存在");
        return reservation;
    }

    private ExamReservationDTO getDetail(Long userId, Long reservationId) {
        ExamReservationDTO detail = examReservationMapper.findReservationDetail(userId, reservationId);
        if (detail == null) throw new IllegalArgumentException("预约不存在");
        return detail;
    }
}