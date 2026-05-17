package org.dep.backend.service;

import org.dep.backend.dto.ExamRegistrationAdminUpdate;
import org.dep.backend.dto.ExamRegistrationApplyRequest;
import org.dep.backend.dto.ExamRegistrationBrief;
import org.dep.backend.dto.ExamRegistrationDTO;
import org.dep.backend.dto.ExamReservationDTO;
import org.dep.backend.dto.ExamScheduleCard;
import org.dep.backend.dto.ExamScheduleSlot;
import org.dep.backend.mapper.ExamRegistrationMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class ExamRegistrationService {
    private static final Map<String, String> PREREQUISITE =
            Map.of("科目二", "科目一", "科目三", "科目二", "科目四", "科目三");
    private static final Set<String> SUBJECT_ORDER = Set.of("科目一", "科目二", "科目三", "科目四");
    private static final Set<String> REG_STATUS = Set.of("pending", "approved", "rejected", "cancelled", "completed");

    private final ExamRegistrationMapper examRegistrationMapper;

    public ExamRegistrationService(ExamRegistrationMapper examRegistrationMapper) {
        this.examRegistrationMapper = examRegistrationMapper;
    }

    public List<ExamScheduleCard> listPublicSchedules(String examType) {
        String normalized = normalizeExamTypeFilter(examType);
        return examRegistrationMapper.listScheduleCards(normalized);
    }

    public List<ExamRegistrationDTO> listMine(Long userId) {
        return examRegistrationMapper.listMine(userId);
    }

    public List<ExamReservationDTO> listAllForAdmin() {
        return examRegistrationMapper.listAllForAdmin();
    }

    @Transactional
    public ExamRegistrationDTO apply(Long userId, ExamRegistrationApplyRequest body) {
        Objects.requireNonNull(userId);
        Long scheduleId = body == null ? null : body.scheduleId();
        if (scheduleId == null) {
            throw new IllegalArgumentException("请选择考试场次 scheduleId");
        }
        ExamScheduleSlot schedule = examRegistrationMapper.loadSchedule(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("考试场次不存在或已下架");
        }
        if (!SUBJECT_ORDER.contains(schedule.examType())) {
            throw new IllegalArgumentException("不支持的考试类型");
        }

        String prereq = PREREQUISITE.get(schedule.examType());
        if (prereq != null && examRegistrationMapper.countPassedSubject(userId, prereq) == 0) {
            throw new IllegalArgumentException(
                    String.format(Locale.CHINA, "报考「%s」前需要先通过「%s」", schedule.examType(), prereq));
        }

        if (examRegistrationMapper.countActiveSameSubject(userId, schedule.examType()) > 0) {
            throw new IllegalArgumentException(
                    String.format(Locale.CHINA, "每个科目仅能保留一场有效预约，请先取消或与管理员联系处理「%s」", schedule.examType()));
        }

        int used = examRegistrationMapper.countActiveRegistrations(scheduleId);
        if (used >= schedule.capacity()) {
            throw new IllegalArgumentException("该场次名额已满，请选择其它时间");
        }

        try {
            examRegistrationMapper.insertRegistration(userId, scheduleId, "");
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("你已预约该场次");
        }

        Long newId = examRegistrationMapper.lastInsertId();
        return examRegistrationMapper.listMine(userId).stream()
                .filter(r -> r.id().equals(newId))
                .findFirst()
                .orElseGet(() ->
                        examRegistrationMapper.listMine(userId).stream()
                                .filter(r -> r.scheduleId().equals(scheduleId))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("预约成功但读取详情失败")));
    }

    @Transactional
    public void cancel(Long userId, Long registrationId) {
        Objects.requireNonNull(userId);
        ExamRegistrationBrief brief = examRegistrationMapper.findBrief(registrationId);
        if (brief == null) {
            throw new IllegalArgumentException("预约记录不存在");
        }
        if (!userId.equals(brief.userId())) {
            throw new IllegalArgumentException("无法取消他人的预约");
        }
        int n = examRegistrationMapper.cancelConfirmedByOwner(registrationId, userId);
        if (n == 0) {
            throw new IllegalArgumentException("仅支持取消待审核/已通过且未开考的预约");
        }
    }

    @Transactional
    public ExamReservationDTO adminUpdate(Long registrationId, ExamRegistrationAdminUpdate body) {
        if (registrationId == null) {
            throw new IllegalArgumentException("registrationId 无效");
        }
        ExamReservationDTO existing = examRegistrationMapper.findAdminRow(registrationId);
        if (existing == null) {
            throw new IllegalArgumentException("报名记录不存在");
        }

        String status = body.status() != null && !body.status().isBlank()
                ? body.status().trim().toLowerCase(Locale.ROOT)
                : existing.status();
        if (!REG_STATUS.contains(status)) {
            throw new IllegalArgumentException("状态必须为 pending / approved / rejected / cancelled / completed");
        }

        Integer score = body.score() != null ? body.score() : existing.score();

        String passed = body.passed();
        if (passed != null) {
            passed = passed.trim().toUpperCase(Locale.ROOT);
            if (!passed.isEmpty() && !"Y".equals(passed) && !"N".equals(passed)) {
                throw new IllegalArgumentException("passed 只能为 Y 或 N（或留空保持原值）");
            }
            if (passed.isEmpty()) {
                passed = existing.passed();
            }
        } else {
            passed = existing.passed();
        }

        String remark = body.remark() != null ? body.remark().trim() : existing.remark();

        examRegistrationMapper.updateAdminFields(registrationId, status, score, passed, remark);
        return Objects.requireNonNull(examRegistrationMapper.findAdminRow(registrationId));
    }

    private String normalizeExamTypeFilter(String examType) {
        if (examType == null || examType.isBlank()) {
            return null;
        }
        String t = examType.trim();
        if (!SUBJECT_ORDER.contains(t)) {
            throw new IllegalArgumentException("科目筛选仅支持：科目一、科目二、科目三、科目四");
        }
        return t;
    }
}
