package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dep.backend.mapper.projection.CoursePackageRow;

import java.util.List;

@Mapper
public interface CoursePackageMapper {
    @Select("""
            SELECT id, code, name, vehicle_type AS vehicleType, price,
                   lesson_hours AS lessonHours, highlights, tag,
                   sort_no AS sortNo, enabled
            FROM course_packages
            WHERE enabled = 1
            ORDER BY sort_no, id
            """)
    List<CoursePackageRow> listPublic();

    @Select("""
            SELECT id, code, name, vehicle_type AS vehicleType, price,
                   lesson_hours AS lessonHours, highlights, tag,
                   sort_no AS sortNo, enabled
            FROM course_packages
            ORDER BY sort_no, id
            """)
    List<CoursePackageRow> listAll();

    @Select("""
            SELECT id, code, name, vehicle_type AS vehicleType, price,
                   lesson_hours AS lessonHours, highlights, tag,
                   sort_no AS sortNo, enabled
            FROM course_packages
            WHERE id = #{id}
            """)
    CoursePackageRow findById(@Param("id") Long id);

    @Insert("""
            INSERT INTO course_packages
              (code, name, vehicle_type, price, lesson_hours, highlights, tag, sort_no, enabled)
            VALUES
              (#{code}, #{name}, #{vehicleType}, #{price}, #{lessonHours}, #{highlights}, #{tag}, #{sortNo}, #{enabled})
            """)
    int insert(@Param("code") String code,
               @Param("name") String name,
               @Param("vehicleType") String vehicleType,
               @Param("price") Integer price,
               @Param("lessonHours") Integer lessonHours,
               @Param("highlights") String highlights,
               @Param("tag") String tag,
               @Param("sortNo") Integer sortNo,
               @Param("enabled") Boolean enabled);

    @Update("""
            UPDATE course_packages
            SET code = #{code},
                name = #{name},
                vehicle_type = #{vehicleType},
                price = #{price},
                lesson_hours = #{lessonHours},
                highlights = #{highlights},
                tag = #{tag},
                sort_no = #{sortNo},
                enabled = #{enabled}
            WHERE id = #{id}
            """)
    int update(@Param("id") Long id,
               @Param("code") String code,
               @Param("name") String name,
               @Param("vehicleType") String vehicleType,
               @Param("price") Integer price,
               @Param("lessonHours") Integer lessonHours,
               @Param("highlights") String highlights,
               @Param("tag") String tag,
               @Param("sortNo") Integer sortNo,
               @Param("enabled") Boolean enabled);

    @Update("UPDATE course_packages SET enabled = #{enabled} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("enabled") Boolean enabled);

    @Delete("DELETE FROM course_packages WHERE id = #{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT LAST_INSERT_ID()")
    Long lastInsertId();
}
