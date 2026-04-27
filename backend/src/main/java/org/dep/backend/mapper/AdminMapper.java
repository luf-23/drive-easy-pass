package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dep.backend.dto.AppRouteDto;
import org.dep.backend.mapper.projection.RoleBaseRecord;

import java.util.List;

@Mapper
public interface AdminMapper {

    @Select("""
            SELECT COUNT(*)
            FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.code = 'admin'
              AND r.enabled = 1
            """)
    Integer countAdminRole(@Param("userId") Long userId);

    @Select({
        "<script>",
        "SELECT COUNT(*)",
        "FROM user_roles ur",
        "JOIN roles r ON r.id = ur.role_id",
        "WHERE ur.user_id = #{userId}",
        "  AND r.enabled = 1",
        "  AND r.code IN",
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
                   component,
                   icon,
                   rank_no AS rankNo,
                   enabled
            FROM app_routes
            ORDER BY rank_no, id
            """)
    List<AppRouteDto> listRoutes();

    @Select("""
            SELECT id,
                   path,
                   name,
                   title,
                   parent_id AS parentId,
                   component,
                   icon,
                   rank_no AS rankNo,
                   enabled
            FROM app_routes
            WHERE id = #{id}
            """)
    AppRouteDto findRouteById(@Param("id") Long id);

    @Insert("""
            INSERT INTO app_routes (path, name, title, parent_id, component, icon, rank_no, enabled)
            VALUES (#{path}, #{name}, #{title}, #{parentId}, #{component}, #{icon}, #{rankNo}, #{enabled})
            """)
    int insertRoute(@Param("path") String path,
                    @Param("name") String name,
                    @Param("title") String title,
                    @Param("parentId") Long parentId,
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
            SELECT id,
                   code,
                   name,
                   description,
                   enabled
            FROM roles
            ORDER BY id
            """)
    List<RoleBaseRecord> listRoles();

    @Select("""
            SELECT id,
                   code,
                   name,
                   description,
                   enabled
            FROM roles
            WHERE id = #{id}
            """)
    RoleBaseRecord findRoleById(@Param("id") Long id);

    @Select("""
            SELECT route_id
            FROM role_routes
            WHERE role_id = #{roleId}
            ORDER BY route_id
            """)
    List<Long> listRouteIdsByRoleId(@Param("roleId") Long roleId);

    @Insert("""
            INSERT INTO roles (code, name, description, enabled)
            VALUES (#{code}, #{name}, #{description}, #{enabled})
            """)
    int insertRole(@Param("code") String code,
                   @Param("name") String name,
                   @Param("description") String description,
                   @Param("enabled") Boolean enabled);

    @Update("""
            UPDATE roles
            SET code = #{code},
                name = #{name},
                description = #{description},
                enabled = #{enabled}
            WHERE id = #{id}
            """)
    int updateRole(@Param("id") Long id,
                   @Param("code") String code,
                   @Param("name") String name,
                   @Param("description") String description,
                   @Param("enabled") Boolean enabled);

    @Delete("DELETE FROM role_routes WHERE role_id = #{roleId}")
    int deleteRoleRoutesByRoleId(@Param("roleId") Long roleId);

    @Insert("""
            INSERT IGNORE INTO role_routes (role_id, route_id)
            VALUES (#{roleId}, #{routeId})
            """)
    int insertRoleRoute(@Param("roleId") Long roleId, @Param("routeId") Long routeId);

    @Delete("DELETE FROM user_roles WHERE role_id = #{roleId}")
    int deleteUserRolesByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM roles WHERE id = #{id}")
    int deleteRoleById(@Param("id") Long id);
}
