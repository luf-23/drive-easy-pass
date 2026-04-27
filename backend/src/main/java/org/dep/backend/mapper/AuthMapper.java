package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dep.backend.model.AppUser;

import java.util.List;

@Mapper
public interface AuthMapper {

    @Insert("""
            INSERT INTO users (username, password_hash, nickname)
            VALUES (#{username}, #{passwordHash}, #{nickname})
            """)
    int insertUser(@Param("username") String username,
                   @Param("passwordHash") String passwordHash,
                   @Param("nickname") String nickname);

    @Select("""
            SELECT id,
                   username,
                   password_hash AS passwordHash,
                   nickname,
                   create_time AS createTime
            FROM users
            WHERE username = #{username}
            """)
    AppUser findByUsername(@Param("username") String username);

    @Select("""
            SELECT id,
                   username,
                   password_hash AS passwordHash,
                   nickname,
                   create_time AS createTime
            FROM users
            WHERE id = #{id}
            """)
    AppUser findById(@Param("id") Long id);

    @Select("""
            SELECT r.code
            FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.enabled = 1
            ORDER BY r.id
            """)
    List<String> findRoleCodes(@Param("userId") Long userId);

    @Insert("""
            INSERT IGNORE INTO user_roles (user_id, role_id)
            SELECT #{userId}, id FROM roles WHERE code = #{roleCode}
            """)
    int assignRoleByCode(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}
