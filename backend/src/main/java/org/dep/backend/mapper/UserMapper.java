package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dep.backend.model.AppUser;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select({
        "<script>",
        "SELECT id, username, password_hash AS passwordHash, nickname, role, email, status, create_time AS createTime",
        "FROM users",
        "<where>",
        "  <if test='username != null and username != \"\"'>",
        "    AND username LIKE CONCAT('%', #{username}, '%')",
        "  </if>",
        "  <if test='email != null and email != \"\"'>",
        "    AND email LIKE CONCAT('%', #{email}, '%')",
        "  </if>",
        "  <if test='role != null and role != \"\"'>",
        "    AND role = #{role}",
        "  </if>",
        "</where>",
        "ORDER BY id DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<AppUser> findUsersByCondition(@Param("username") String username, 
                                       @Param("email") String email, 
                                       @Param("role") String role, 
                                       @Param("offset") int offset, 
                                       @Param("pageSize") int pageSize);

    @Select({
        "<script>",
        "SELECT COUNT(*)",
        "FROM users",
        "<where>",
        "  <if test='username != null and username != \"\"'>",
        "    AND username LIKE CONCAT('%', #{username}, '%')",
        "  </if>",
        "  <if test='email != null and email != \"\"'>",
        "    AND email LIKE CONCAT('%', #{email}, '%')",
        "  </if>",
        "  <if test='role != null and role != \"\"'>",
        "    AND role = #{role}",
        "  </if>",
        "</where>",
        "</script>"
    })
    long countUsersByCondition(@Param("username") String username, 
                               @Param("email") String email, 
                               @Param("role") String role);

    @Select("SELECT id, username, password_hash AS passwordHash, nickname, role, email, status, create_time AS createTime FROM users WHERE id = #{id}")
    AppUser findById(@Param("id") Long id);

    @Insert("INSERT INTO users (username, password_hash, nickname, role, email, status, create_time) " +
            "VALUES (#{username}, #{passwordHash}, #{nickname}, #{role}, #{email}, COALESCE(#{status}, 1), NOW())")
    void insert(AppUser user);

    @Update("UPDATE users SET username = #{username}, nickname = #{nickname}, role = #{role}, email = #{email} WHERE id = #{id}")
    void update(AppUser user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE users SET password_hash = #{passwordHash} WHERE id = #{id}")
    void updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    @Select("SELECT id, username, password_hash AS passwordHash, nickname, role, email, status, create_time AS createTime FROM users WHERE username = #{username}")
    AppUser findByUsername(@Param("username") String username);
}