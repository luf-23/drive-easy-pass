package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dep.backend.dto.ExamScheduleDTO;
import org.dep.backend.model.ExamRouteDetail;
import org.dep.backend.model.ExamVenue;

import java.util.List;

@Mapper
public interface ExamVenueMapper {

    @Select("""
            SELECT id,
                   venue_name AS venueName,
                   venue_code AS venueCode,
                   address,
                   district,
                   contact_phone AS contactPhone,
                   exam_type AS examType,
                   total_slots AS totalSlots,
                   available_slots AS availableSlots,
                   route_description AS routeDescription,
                   route_map_url AS routeMapUrl,
                   facilities,
                   business_hours AS businessHours,
                   longitude,
                   latitude,
                   status,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM exam_venues
            WHERE status = 'ACTIVE'
            ORDER BY id
            """)
    List<ExamVenue> listActiveVenues();

    @Select("""
            SELECT id,
                   venue_name AS venueName,
                   venue_code AS venueCode,
                   address,
                   district,
                   contact_phone AS contactPhone,
                   exam_type AS examType,
                   total_slots AS totalSlots,
                   available_slots AS availableSlots,
                   route_description AS routeDescription,
                   route_map_url AS routeMapUrl,
                   facilities,
                   business_hours AS businessHours,
                   longitude,
                   latitude,
                   status,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM exam_venues
            WHERE district = #{district}
              AND status = 'ACTIVE'
            """)
    List<ExamVenue> listVenuesByDistrict(@Param("district") String district);

    @Select("""
            SELECT id,
                   venue_name AS venueName,
                   venue_code AS venueCode,
                   address,
                   district,
                   contact_phone AS contactPhone,
                   exam_type AS examType,
                   total_slots AS totalSlots,
                   available_slots AS availableSlots,
                   route_description AS routeDescription,
                   route_map_url AS routeMapUrl,
                   facilities,
                   business_hours AS businessHours,
                   longitude,
                   latitude,
                   status,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM exam_venues
            WHERE exam_type = #{examType}
              AND status = 'ACTIVE'
            """)
    List<ExamVenue> listVenuesByExamType(@Param("examType") String examType);

    @Select("""
            SELECT id,
                   venue_name AS venueName,
                   venue_code AS venueCode,
                   address,
                   district,
                   contact_phone AS contactPhone,
                   exam_type AS examType,
                   total_slots AS totalSlots,
                   available_slots AS availableSlots,
                   route_description AS routeDescription,
                   route_map_url AS routeMapUrl,
                   facilities,
                   business_hours AS businessHours,
                   longitude,
                   latitude,
                   status,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM exam_venues
            WHERE id = #{venueId}
            """)
    ExamVenue findVenueById(@Param("venueId") Long venueId);

    @Select("""
            SELECT id,
                   venue_name AS venueName,
                   venue_code AS venueCode,
                   address,
                   district,
                   contact_phone AS contactPhone,
                   exam_type AS examType,
                   total_slots AS totalSlots,
                   available_slots AS availableSlots,
                   route_description AS routeDescription,
                   route_map_url AS routeMapUrl,
                   facilities,
                   business_hours AS businessHours,
                   longitude,
                   latitude,
                   status,
                   create_time AS createTime,
                   update_time AS updateTime
            FROM exam_venues
            WHERE (venue_name LIKE CONCAT('%', #{keyword}, '%') OR address LIKE CONCAT('%', #{keyword}, '%'))
              AND status = 'ACTIVE'
            """)
    List<ExamVenue> searchVenues(@Param("keyword") String keyword);

    @Select("""
            SELECT id,
                   venue_id AS venueId,
                   route_name AS routeName,
                   route_number AS routeNumber,
                   description,
                   start_point AS startPoint,
                   end_point AS endPoint,
                   distance,
                   difficulty,
                   points,
                   map_image_url AS mapImageUrl,
                   sort_order AS sortOrder,
                   create_time AS createTime
            FROM exam_route_details
            WHERE venue_id = #{venueId}
            ORDER BY sort_order
            """)
    List<ExamRouteDetail> listVenueRoutes(@Param("venueId") Long venueId);

    @Select("""
            SELECT id,
                   venue_id AS venueId,
                   route_name AS routeName,
                   route_number AS routeNumber,
                   description,
                   start_point AS startPoint,
                   end_point AS endPoint,
                   distance,
                   difficulty,
                   points,
                   map_image_url AS mapImageUrl,
                   sort_order AS sortOrder,
                   create_time AS createTime
            FROM exam_route_details
            WHERE id = #{routeId}
            """)
    ExamRouteDetail findRouteById(@Param("routeId") Long routeId);

    @Select({
            "<script>",
            "SELECT s.id,",
            "       s.venue_id AS venueId,",
            "       v.venue_name AS venueName,",
            "       s.exam_date AS examDate,",
            "       s.start_time AS startTime,",
            "       s.end_time AS endTime,",
            "       s.exam_type AS examType,",
            "       s.total_slots AS totalSlots,",
            "       s.reserved_slots AS reservedSlots,",
            "       (s.total_slots - s.reserved_slots) AS availableSlots,",
            "       s.status",
            "FROM exam_schedules s",
            "JOIN exam_venues v ON s.venue_id = v.id",
            "WHERE s.venue_id = #{venueId}",
            "  AND s.status = 'OPEN'",
            "  AND s.exam_date >= CURDATE()",
            "<if test='examType != null and examType != \"\"'>",
            "  AND s.exam_type = #{examType}",
            "</if>",
            "ORDER BY s.exam_date, s.start_time",
            "</script>"
    })
    List<ExamScheduleDTO> listVenueSchedules(@Param("venueId") Long venueId,
                                             @Param("examType") String examType);

    @Select({
            "<script>",
            "SELECT s.id,",
            "       s.venue_id AS venueId,",
            "       v.venue_name AS venueName,",
            "       s.exam_date AS examDate,",
            "       s.start_time AS startTime,",
            "       s.end_time AS endTime,",
            "       s.exam_type AS examType,",
            "       s.total_slots AS totalSlots,",
            "       s.reserved_slots AS reservedSlots,",
            "       (s.total_slots - s.reserved_slots) AS availableSlots,",
            "       s.status",
            "FROM exam_schedules s",
            "JOIN exam_venues v ON s.venue_id = v.id",
            "WHERE s.status = 'OPEN'",
            "  AND s.exam_date >= CURDATE()",
            "<if test='examType != null and examType != \"\"'>",
            "  AND s.exam_type = #{examType}",
            "</if>",
            "<if test='district != null and district != \"\"'>",
            "  AND v.district = #{district}",
            "</if>",
            "ORDER BY v.venue_name, s.exam_date, s.start_time",
            "LIMIT 100",
            "</script>"
    })
    List<ExamScheduleDTO> listAvailableSchedules(@Param("examType") String examType,
                                                 @Param("district") String district);
}
