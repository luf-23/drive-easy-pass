package org.dep.backend.dto;

import java.util.List;

public record EnrollmentDashboardDto(
        Long todayNewLeads,
        Double monthConversionRate,
        List<EnrollmentSourceStatDto> sourceDistribution,
        List<EnrollmentOwnerPerformanceDto> ownerRanking,
        List<EnrollmentIntentStatDto> intentDistribution,
        List<EnrollmentFunnelStatDto> funnel
) {
}
