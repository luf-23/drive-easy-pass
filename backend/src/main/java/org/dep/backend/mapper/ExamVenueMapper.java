package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dep.backend.dto.ExamVenueBase;
import org.dep.backend.dto.ExamVenueRouteItem;
import org.dep.backend.dto.ExamVenueSummary;

import java.util.List;

@Mapper
public interface ExamVenueMapper {

    @Select("""
            SELECT ev.id,
                   ev.name,
                   ev.address,
                   ev.contact_phone AS contactPhone,
                   (EXISTS(
                     SELECT 1 FROM exam_venue_routes r
                     WHERE r.venue_id = ev.id
                       AND r.exam_type = '科目二'
                       AND r.enabled = 1
                       AND (r.route_url <> '' OR (r.route_path IS NOT NULL AND r.route_path <> ''))
                   ) = 1) AS hasSubject2Route,
                   (EXISTS(
                     SELECT 1 FROM exam_venue_routes r
                     WHERE r.venue_id = ev.id
                       AND r.exam_type = '科目三'
                       AND r.enabled = 1
                       AND (r.route_url <> '' OR (r.route_path IS NOT NULL AND r.route_path <> ''))
                   ) = 1) AS hasSubject3Route
            FROM exam_venues ev
            ORDER BY ev.id
            """)
    List<ExamVenueSummary> listSummaries();

    @Select("""
            SELECT id,
                   name,
                   address,
                   contact_phone AS contactPhone
            FROM exam_venues
            ORDER BY id
            """)
    List<ExamVenueBase> listVenuesOnly();

    @Select("""
            SELECT id,
                   name,
                   address,
                   contact_phone AS contactPhone
            FROM exam_venues
            WHERE id = #{venueId}
            """)
    ExamVenueBase findVenueBase(@Param("venueId") Long venueId);

    @Select("""
            SELECT exam_type AS examType,
                   media_type AS mediaType,
                   title,
                   route_url AS routeUrl,
                   route_path AS routePath,
                   cover_url AS coverUrl,
                   remark,
                   (enabled = 1) AS enabled
            FROM exam_venue_routes
            WHERE venue_id = #{venueId}
              AND exam_type IN ('科目二', '科目三')
            ORDER BY exam_type
            """)
    List<ExamVenueRouteItem> listRoutes(@Param("venueId") Long venueId);

    @Delete("DELETE FROM exam_venue_routes WHERE venue_id = #{venueId}")
    int deleteRoutes(@Param("venueId") Long venueId);

    @Insert("""
            INSERT INTO exam_venue_routes (
              venue_id, exam_type, media_type, title, route_url, route_path, cover_url, remark, sort_no, enabled
            ) VALUES (
              #{venueId}, #{examType}, #{mediaType}, #{title}, #{routeUrl}, #{routePath}, #{coverUrl}, #{remark}, #{sortNo}, #{enabled}
            )
            """)
    int insertRoute(
            @Param("venueId") Long venueId,
            @Param("examType") String examType,
            @Param("mediaType") String mediaType,
            @Param("title") String title,
            @Param("routeUrl") String routeUrl,
            @Param("routePath") String routePath,
            @Param("coverUrl") String coverUrl,
            @Param("remark") String remark,
            @Param("sortNo") int sortNo,
            @Param("enabled") int enabled
    );
}
