package org.dep.backend.mapper.projection;

public record CoursePackageRow(
        Long id,
        String code,
        String name,
        String vehicleType,
        Integer price,
        Integer lessonHours,
        String highlights,
        String tag,
        Integer sortNo,
        Boolean enabled
) {
}
