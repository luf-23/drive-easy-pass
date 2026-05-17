package org.dep.backend.service;

import org.dep.backend.dto.EnrollmentDashboardDto;
import org.dep.backend.dto.EnrollmentFollowUpDto;
import org.dep.backend.dto.EnrollmentFollowUpRequest;
import org.dep.backend.dto.EnrollmentLeadDto;
import org.dep.backend.dto.EnrollmentLeadRequest;
import org.dep.backend.dto.PageResult;
import org.dep.backend.dto.PublicEnrollmentIntentRequest;
import org.dep.backend.mapper.EnrollmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EnrollmentService {
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentService(EnrollmentMapper enrollmentMapper) {
        this.enrollmentMapper = enrollmentMapper;
    }

    public PageResult<EnrollmentLeadDto> list(String keyword, String status, String source,
                                              String startDate, String endDate,
                                              Integer page, Integer pageSize) {
        int safePage = Math.max(page == null ? 1 : page, 1);
        int safePageSize = Math.min(Math.max(pageSize == null ? 10 : pageSize, 1), 1000);
        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        List<EnrollmentLeadDto> items = enrollmentMapper.listLeads(
                trimToNull(keyword), trimToNull(status), trimToNull(source),
                start, end, safePageSize, (safePage - 1) * safePageSize
        );
        long total = enrollmentMapper.countLeads(
                trimToNull(keyword), trimToNull(status), trimToNull(source), start, end
        );
        return new PageResult<>(items, total);
    }

    public PageResult<EnrollmentLeadDto> listSigned(String keyword, Integer page, Integer pageSize) {
        return list(keyword, "已报名", null, null, null, page, pageSize);
    }

    public EnrollmentLeadDto create(EnrollmentLeadRequest request) {
        validateLead(request.name(), request.phone());
        enrollmentMapper.insertLead(
                norm(request.name()),
                norm(request.phone()),
                blankDefault(request.source(), "其他"),
                blankDefault(request.intentLevel(), "中"),
                blankDefault(request.status(), "待跟进"),
                request.ownerUserId(),
                parseDateTime(request.nextFollowTime()),
                blankDefault(request.remark(), "")
        );
        return requireLead(enrollmentMapper.lastInsertId());
    }

    public EnrollmentLeadDto createPublic(PublicEnrollmentIntentRequest request) {
        String remark = String.format("车型: %s; 班型: %s; 备注: %s",
                blankDefault(request.vehicleType(), "-"),
                blankDefault(request.classType(), "-"),
                blankDefault(request.remark(), ""));
        EnrollmentLeadRequest body = new EnrollmentLeadRequest(
                request.name(),
                request.phone(),
                blankDefault(request.source(), "线上报名"),
                "中",
                "待跟进",
                null,
                null,
                remark
        );
        return create(body);
    }

    public EnrollmentLeadDto update(Long id, EnrollmentLeadRequest request) {
        validateLead(request.name(), request.phone());
        enrollmentMapper.updateLead(
                id,
                norm(request.name()),
                norm(request.phone()),
                blankDefault(request.source(), "其他"),
                blankDefault(request.intentLevel(), "中"),
                blankDefault(request.status(), "待跟进"),
                request.ownerUserId(),
                parseDateTime(request.nextFollowTime()),
                blankDefault(request.remark(), "")
        );
        return requireLead(id);
    }

    public EnrollmentLeadDto assignOwner(Long id, Long ownerUserId) {
        if (ownerUserId == null || ownerUserId <= 0) {
            throw new IllegalArgumentException("ownerUserId is required");
        }
        enrollmentMapper.assignOwner(id, ownerUserId);
        return requireLead(id);
    }

    public EnrollmentLeadDto updateStatus(Long id, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        enrollmentMapper.updateStatus(id, status.trim());
        return requireLead(id);
    }

    public EnrollmentLeadDto convertToStudent(Long id) {
        enrollmentMapper.updateStatus(id, "已报名");
        return requireLead(id);
    }

    public List<EnrollmentFollowUpDto> followUps(Long leadId) {
        requireLead(leadId);
        return enrollmentMapper.listFollowUps(leadId);
    }

    @Transactional
    public EnrollmentFollowUpDto addFollowUp(Long leadId, EnrollmentFollowUpRequest request, Long creatorUserId) {
        requireLead(leadId);
        if (request == null || request.content() == null || request.content().trim().isBlank()) {
            throw new IllegalArgumentException("Follow-up content is required");
        }
        enrollmentMapper.insertFollowUp(
                leadId,
                request.content().trim(),
                blankDefault(request.followType(), "电话"),
                parseDateTime(request.nextFollowTime()),
                creatorUserId
        );
        if (request.nextFollowTime() != null && !request.nextFollowTime().isBlank()) {
            EnrollmentLeadDto lead = requireLead(leadId);
            enrollmentMapper.updateLead(
                    lead.id(), lead.name(), lead.phone(), lead.source(), lead.intentLevel(), lead.status(),
                    lead.ownerUserId(), parseDateTime(request.nextFollowTime()), lead.remark()
            );
        }
        return enrollmentMapper.listFollowUps(leadId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Follow-up saved but not found"));
    }

    public EnrollmentDashboardDto dashboard() {
        long all = enrollmentMapper.countAllLeads();
        long signed = enrollmentMapper.countSignedLeads();
        double conversion = all == 0 ? 0 : signed * 100.0 / all;
        return new EnrollmentDashboardDto(
                enrollmentMapper.countTodayLeads(),
                conversion,
                enrollmentMapper.sourceStats(),
                enrollmentMapper.ownerRanking(),
                enrollmentMapper.intentStats(),
                enrollmentMapper.funnelStats()
        );
    }

    private EnrollmentLeadDto requireLead(Long id) {
        EnrollmentLeadDto lead = enrollmentMapper.findLead(id);
        if (lead == null) {
            throw new IllegalArgumentException("Enrollment lead not found");
        }
        return lead;
    }

    private void validateLead(String name, String phone) {
        if (norm(name).isBlank() || norm(phone).isBlank()) {
            throw new IllegalArgumentException("Name and phone are required");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() == 16) {
            normalized = normalized + ":00";
        }
        return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private String trimToNull(String value) {
        String v = norm(value);
        return v.isBlank() ? null : v;
    }

    private String norm(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankDefault(String value, String fallback) {
        String v = norm(value);
        return v.isBlank() ? fallback : v;
    }
}
