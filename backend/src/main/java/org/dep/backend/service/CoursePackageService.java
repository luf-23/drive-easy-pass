package org.dep.backend.service;

import org.dep.backend.dto.CoursePackageDto;
import org.dep.backend.dto.CoursePackageRequest;
import org.dep.backend.mapper.CoursePackageMapper;
import org.dep.backend.mapper.projection.CoursePackageRow;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CoursePackageService {
    private final CoursePackageMapper coursePackageMapper;

    public CoursePackageService(CoursePackageMapper coursePackageMapper) {
        this.coursePackageMapper = coursePackageMapper;
    }

    public List<CoursePackageDto> listPublic() {
        return coursePackageMapper.listPublic().stream().map(this::toDto).toList();
    }

    public List<CoursePackageDto> listAll() {
        return coursePackageMapper.listAll().stream().map(this::toDto).toList();
    }

    public CoursePackageDto create(CoursePackageRequest request) {
        validate(request);
        coursePackageMapper.insert(
                norm(request.code()),
                norm(request.name()),
                blankDefault(request.vehicleType(), "C1"),
                request.price() == null ? 0 : request.price(),
                request.lessonHours() == null ? 0 : request.lessonHours(),
                joinHighlights(request.highlights()),
                blankDefault(request.tag(), ""),
                request.sortNo() == null ? 0 : request.sortNo(),
                request.enabled() == null || request.enabled()
        );
        return find(coursePackageMapper.lastInsertId());
    }

    public CoursePackageDto update(Long id, CoursePackageRequest request) {
        validate(request);
        coursePackageMapper.update(
                id,
                norm(request.code()),
                norm(request.name()),
                blankDefault(request.vehicleType(), "C1"),
                request.price() == null ? 0 : request.price(),
                request.lessonHours() == null ? 0 : request.lessonHours(),
                joinHighlights(request.highlights()),
                blankDefault(request.tag(), ""),
                request.sortNo() == null ? 0 : request.sortNo(),
                request.enabled() == null || request.enabled()
        );
        return find(id);
    }

    public CoursePackageDto updateStatus(Long id, Boolean enabled) {
        coursePackageMapper.updateStatus(id, enabled != null && enabled);
        return find(id);
    }

    public void delete(Long id) {
        coursePackageMapper.delete(id);
    }

    private CoursePackageDto find(Long id) {
        CoursePackageRow row = coursePackageMapper.findById(id);
        if (row == null) {
            throw new IllegalArgumentException("Course package not found");
        }
        return toDto(row);
    }

    private CoursePackageDto toDto(CoursePackageRow row) {
        return new CoursePackageDto(
                row.id(),
                row.code(),
                row.name(),
                row.vehicleType(),
                row.price(),
                row.lessonHours(),
                splitHighlights(row.highlights()),
                row.tag(),
                row.sortNo(),
                row.enabled()
        );
    }

    private List<String> splitHighlights(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split("\\|"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private String joinHighlights(List<String> values) {
        if (values == null) return "";
        return String.join("|", values.stream().map(this::norm).filter(v -> !v.isBlank()).toList());
    }

    private void validate(CoursePackageRequest request) {
        if (request == null || norm(request.code()).isBlank() || norm(request.name()).isBlank()) {
            throw new IllegalArgumentException("Course package code and name are required");
        }
    }

    private String norm(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankDefault(String value, String fallback) {
        String v = norm(value);
        return v.isBlank() ? fallback : v;
    }
}
