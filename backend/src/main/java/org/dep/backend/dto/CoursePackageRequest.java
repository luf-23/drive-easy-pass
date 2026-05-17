package org.dep.backend.dto;

import java.util.List;

public record CoursePackageRequest(
        String code,
        String name,
        String vehicleType,
        Integer price,
        Integer lessonHours,
        List<String> highlights,
        String tag,
        Integer sortNo,
        Boolean enabled
) {
}
