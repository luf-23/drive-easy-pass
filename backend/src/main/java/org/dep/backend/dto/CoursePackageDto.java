package org.dep.backend.dto;

import java.util.List;

public record CoursePackageDto(
        String code,
        String name,
        Integer price,
        Integer lessonHours,
        List<String> highlights,
        String tag
) {
}
