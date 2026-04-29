package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dep.backend.dto.AppRouteDto;

import java.util.List;

@Mapper
public interface AdminMapper {

    @Select("""
            SELECT role
            FROM users
            WHERE id = #{userId}
            """)
    String findUserRole(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM users
            WHERE id = #{userId}
              AND role = 'admin'
            """)
    Integer countAdminRole(@Param("userId") Long userId);

    @Select({
        "<script>",
        "SELECT COUNT(*)",
        "FROM users",
        "WHERE id = #{userId}",
        "  AND role IN",
        "  <foreach item='item' collection='roleCodes' open='(' separator=',' close=')'>",
        "    #{item}",
        "  </foreach>",
        "</script>"
    })
    Integer countAnyRole(@Param("userId") Long userId, @Param("roleCodes") List<String> roleCodes);

    @Select("""
            SELECT id,
                   path,
                   name,
                   title,
                   parent_id AS parentId,
                   redirect,
                   component,
                   icon,
                   rank_no AS rankNo,
                   enabled
            FROM app_routes
            ORDER BY rank_no, id
            """)
    List<AppRouteDto> listRoutes();

    @Select("""
            SELECT ar.id,
                   ar.path,
                   ar.name,
                   ar.title,
                   ar.parent_id AS parentId,
                   ar.redirect,
                   ar.component,
                   ar.icon,
                   ar.rank_no AS rankNo,
                   ar.enabled
            FROM app_routes ar
            JOIN role_routes rr ON rr.route_id = ar.id
            WHERE rr.role = #{role}
              AND ar.enabled = 1
            ORDER BY ar.rank_no, ar.id
            """)
    List<AppRouteDto> listRoutesByRole(@Param("role") String role);

    @Select("""
            SELECT id,
                   path,
                   name,
                   title,
                   parent_id AS parentId,
                   redirect,
                   component,
                   icon,
                   rank_no AS rankNo,
                   enabled
            FROM app_routes
            WHERE id = #{id}
            """)
    AppRouteDto findRouteById(@Param("id") Long id);

    @Insert("""
            INSERT INTO app_routes (path, name, title, parent_id, redirect, component, icon, rank_no, enabled)
            VALUES (#{path}, #{name}, #{title}, #{parentId}, #{redirect}, #{component}, #{icon}, #{rankNo}, #{enabled})
            """)
    int insertRoute(@Param("path") String path,
                    @Param("name") String name,
                    @Param("title") String title,
                    @Param("parentId") Long parentId,
                    @Param("redirect") String redirect,
                    @Param("component") String component,
                    @Param("icon") String icon,
                    @Param("rankNo") Integer rankNo,
                    @Param("enabled") Boolean enabled);

    @Update("""
            UPDATE app_routes
            SET path = #{path},
                name = #{name},
                title = #{title},
                parent_id = #{parentId},
                redirect = #{redirect},
                component = #{component},
                icon = #{icon},
                rank_no = #{rankNo},
                enabled = #{enabled}
            WHERE id = #{id}
            """)
    int updateRoute(@Param("id") Long id,
                    @Param("path") String path,
                    @Param("name") String name,
                    @Param("title") String title,
                    @Param("parentId") Long parentId,
                    @Param("redirect") String redirect,
                    @Param("component") String component,
                    @Param("icon") String icon,
                    @Param("rankNo") Integer rankNo,
                    @Param("enabled") Boolean enabled);

    @Delete("DELETE FROM role_routes WHERE route_id = #{routeId}")
    int deleteRoleRoutesByRouteId(@Param("routeId") Long routeId);

    @Delete("DELETE FROM app_routes WHERE id = #{id}")
    int deleteRouteById(@Param("id") Long id);

    @Select("SELECT LAST_INSERT_ID()")
    Long lastInsertId();

    @Select("""
            SELECT route_id
            FROM role_routes
            WHERE role = #{role}
            ORDER BY route_id
            """)
    List<Long> listRouteIdsByRole(@Param("role") String role);

    @Delete("DELETE FROM role_routes WHERE role = #{role}")
    int deleteRoleRoutesByRole(@Param("role") String role);

    @Insert("""
            INSERT IGNORE INTO role_routes (role, route_id)
            VALUES (#{role}, #{routeId})
            """)
    int insertRoleRoute(@Param("role") String role, @Param("routeId") Long routeId);
}
