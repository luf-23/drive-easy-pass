package org.dep.backend.service;

import org.dep.backend.dto.ExamVenueBase;
import org.dep.backend.dto.ExamVenueDetail;
import org.dep.backend.dto.ExamVenueRouteItem;
import org.dep.backend.dto.ExamVenueRoutesUpdateRequest;
import org.dep.backend.dto.ExamVenueSummary;
import org.dep.backend.mapper.ExamVenueMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ExamVenueService {
    private static final Set<String> ROUTE_EXAM_TYPES = Set.of("科目二", "科目三");
    private static final Set<String> MEDIA_TYPES = Set.of("map", "video", "image", "gpx", "article");

    private final ExamVenueMapper examVenueMapper;

    public ExamVenueService(ExamVenueMapper examVenueMapper) {
        this.examVenueMapper = examVenueMapper;
    }

    public List<ExamVenueSummary> listPublic() {
        return safeListSummaries();
    }

    private List<ExamVenueSummary> safeListSummaries() {
        try {
            return examVenueMapper.listSummaries();
        } catch (DataAccessException ex) {
            return listSummariesWithoutRoutes();
        }
    }

    private List<ExamVenueSummary> listSummariesWithoutRoutes() {
        return examVenueMapper.listVenuesOnly().stream()
                .map(v -> new ExamVenueSummary(
                        v.id(), v.name(), v.address(), v.contactPhone(), false, false))
                .toList();
    }

    private List<ExamVenueRouteItem> safeListRoutes(Long venueId) {
        try {
            return examVenueMapper.listRoutes(venueId);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    public ExamVenueDetail getDetail(Long venueId) {
        ExamVenueBase base = examVenueMapper.findVenueBase(venueId);
        if (base == null) {
            throw new IllegalArgumentException("考场不存在");
        }
        List<ExamVenueRouteItem> routes = normalizeRoutes(safeListRoutes(venueId));
        return new ExamVenueDetail(
                base.id(),
                base.name(),
                base.address(),
                base.contactPhone(),
                routes
        );
    }

    @Transactional
    public ExamVenueDetail saveRoutes(Long venueId, ExamVenueRoutesUpdateRequest body) {
        ExamVenueBase base = examVenueMapper.findVenueBase(venueId);
        if (base == null) {
            throw new IllegalArgumentException("考场不存在");
        }
        List<ExamVenueRouteItem> incoming = body == null || body.routes() == null ? List.of() : body.routes();
        Map<String, ExamVenueRouteItem> byExamType = new LinkedHashMap<>();
        for (ExamVenueRouteItem item : incoming) {
            ExamVenueRouteItem validated = validateRouteItem(item);
            byExamType.put(validated.examType(), validated);
        }
        List<ExamVenueRouteItem> validated = new ArrayList<>(byExamType.values());
        examVenueMapper.deleteRoutes(venueId);
        int sort = 0;
        for (ExamVenueRouteItem item : validated) {
            if (!item.enabled() && isBlank(item.routeUrl()) && isBlank(item.routePath())) {
                continue;
            }
            examVenueMapper.insertRoute(
                    venueId,
                    item.examType(),
                    item.mediaType(),
                    nullToEmpty(item.title()),
                    nullToEmpty(item.routeUrl()),
                    nullToEmpty(item.routePath()),
                    nullToEmpty(item.coverUrl()),
                    nullToEmpty(item.remark()),
                    sort++,
                    item.enabled() ? 1 : 0
            );
        }
        return getDetail(venueId);
    }

    private ExamVenueRouteItem validateRouteItem(ExamVenueRouteItem item) {
        if (item == null) {
            throw new IllegalArgumentException("路线条目不能为空");
        }
        String examType = trim(item.examType());
        if (!ROUTE_EXAM_TYPES.contains(examType)) {
            throw new IllegalArgumentException("仅支持维护科目二、科目三的考场路线");
        }
        String mediaType = trim(item.mediaType());
        if (mediaType.isEmpty()) {
            mediaType = "map";
        }
        if (!MEDIA_TYPES.contains(mediaType)) {
            throw new IllegalArgumentException("不支持的媒体类型: " + mediaType);
        }
        boolean enabled = Boolean.TRUE.equals(item.enabled());
        String routeUrl = nullToEmpty(item.routeUrl());
        String routePath = nullToEmpty(item.routePath());
        if (enabled && routeUrl.isEmpty() && routePath.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(Locale.CHINA, "「%s」启用时需填写地图轨迹 routePath 或路线链接", examType));
        }
        return new ExamVenueRouteItem(
                examType,
                mediaType,
                nullToEmpty(item.title()),
                routeUrl,
                routePath,
                nullToEmpty(item.coverUrl()),
                nullToEmpty(item.remark()),
                enabled
        );
    }

    private List<ExamVenueRouteItem> normalizeRoutes(List<ExamVenueRouteItem> stored) {
        List<ExamVenueRouteItem> result = new ArrayList<>();
        for (String examType : List.of("科目二", "科目三")) {
            ExamVenueRouteItem found = stored == null
                    ? null
                    : stored.stream().filter(r -> examType.equals(r.examType())).findFirst().orElse(null);
            if (found != null) {
                result.add(found);
            } else {
                result.add(emptyRoute(examType));
            }
        }
        return result;
    }

    private ExamVenueRouteItem emptyRoute(String examType) {
        return new ExamVenueRouteItem(examType, "map", "", "", "", "", "", Boolean.FALSE);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
