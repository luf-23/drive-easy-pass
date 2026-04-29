package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dep.backend.model.AppUser;

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
                   role,
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
                   role,
                   create_time AS createTime
            FROM users
            WHERE id = #{id}
            """)
    AppUser findById(@Param("id") Long id);
}
