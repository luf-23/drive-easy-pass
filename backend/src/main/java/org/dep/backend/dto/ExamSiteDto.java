package org.dep.backend.dto;

import java.util.List;

public record ExamSiteDto(
        String code,
        String name,
        String address,
        List<String> subjects,
        String imageUrl,
        String routeDescription
) {
}
