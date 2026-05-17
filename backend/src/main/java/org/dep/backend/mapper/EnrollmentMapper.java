package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dep.backend.dto.EnrollmentFollowUpDto;
import org.dep.backend.dto.EnrollmentFunnelStatDto;
import org.dep.backend.dto.EnrollmentIntentStatDto;
import org.dep.backend.dto.EnrollmentLeadDto;
import org.dep.backend.dto.EnrollmentOwnerPerformanceDto;
import org.dep.backend.dto.EnrollmentSourceStatDto;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EnrollmentMapper {
    @Select({
            "<script>",
            "SELECT ei.id, ei.name, ei.phone, ei.source, ei.intent_level AS intentLevel,",
            "       ei.status, ei.owner_user_id AS ownerUserId, u.nickname AS ownerName,",
            "       ei.next_follow_time AS nextFollowTime, ei.remark,",
            "       ei.create_time AS createTime, ei.update_time AS updateTime",
            "FROM enrollment_intents ei",
            "LEFT JOIN users u ON u.id = ei.owner_user_id",
            "WHERE 1 = 1",
            "<if test='keyword != null and keyword != \"\"'>",
            "  AND (ei.name LIKE CONCAT('%', #{keyword}, '%') OR ei.phone LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='status != null and status != \"\"'>",
            "  AND ei.status = #{status}",
            "</if>",
            "<if test='source != null and source != \"\"'>",
            "  AND ei.source = #{source}",
            "</if>",
            "<if test='startDate != null'>",
            "  AND ei.create_time &gt;= #{startDate}",
            "</if>",
            "<if test='endDate != null'>",
            "  AND ei.create_time &lt;= #{endDate}",
            "</if>",
            "ORDER BY ei.update_time DESC, ei.id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<EnrollmentLeadDto> listLeads(@Param("keyword") String keyword,
                                      @Param("status") String status,
                                      @Param("source") String source,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      @Param("limit") int limit,
                                      @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM enrollment_intents ei",
            "WHERE 1 = 1",
            "<if test='keyword != null and keyword != \"\"'>",
            "  AND (ei.name LIKE CONCAT('%', #{keyword}, '%') OR ei.phone LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='status != null and status != \"\"'>",
            "  AND ei.status = #{status}",
            "</if>",
            "<if test='source != null and source != \"\"'>",
            "  AND ei.source = #{source}",
            "</if>",
            "<if test='startDate != null'>",
            "  AND ei.create_time &gt;= #{startDate}",
            "</if>",
            "<if test='endDate != null'>",
            "  AND ei.create_time &lt;= #{endDate}",
            "</if>",
            "</script>"
    })
    long countLeads(@Param("keyword") String keyword,
                    @Param("status") String status,
                    @Param("source") String source,
                    @Param("startDate") LocalDateTime startDate,
                    @Param("endDate") LocalDateTime endDate);

    @Select("""
            SELECT ei.id, ei.name, ei.phone, ei.source, ei.intent_level AS intentLevel,
                   ei.status, ei.owner_user_id AS ownerUserId, u.nickname AS ownerName,
                   ei.next_follow_time AS nextFollowTime, ei.remark,
                   ei.create_time AS createTime, ei.update_time AS updateTime
            FROM enrollment_intents ei
            LEFT JOIN users u ON u.id = ei.owner_user_id
            WHERE ei.id = #{id}
            """)
    EnrollmentLeadDto findLead(@Param("id") Long id);

    @Insert("""
            INSERT INTO enrollment_intents
              (name, phone, source, intent_level, status, owner_user_id, next_follow_time, remark)
            VALUES
              (#{name}, #{phone}, #{source}, #{intentLevel}, #{status}, #{ownerUserId}, #{nextFollowTime}, #{remark})
            """)
    int insertLead(@Param("name") String name,
                   @Param("phone") String phone,
                   @Param("source") String source,
                   @Param("intentLevel") String intentLevel,
                   @Param("status") String status,
                   @Param("ownerUserId") Long ownerUserId,
                   @Param("nextFollowTime") LocalDateTime nextFollowTime,
                   @Param("remark") String remark);

    @Update("""
            UPDATE enrollment_intents
            SET name = #{name},
                phone = #{phone},
                source = #{source},
                intent_level = #{intentLevel},
                status = #{status},
                owner_user_id = #{ownerUserId},
                next_follow_time = #{nextFollowTime},
                remark = #{remark}
            WHERE id = #{id}
            """)
    int updateLead(@Param("id") Long id,
                   @Param("name") String name,
                   @Param("phone") String phone,
                   @Param("source") String source,
                   @Param("intentLevel") String intentLevel,
                   @Param("status") String status,
                   @Param("ownerUserId") Long ownerUserId,
                   @Param("nextFollowTime") LocalDateTime nextFollowTime,
                   @Param("remark") String remark);

    @Update("UPDATE enrollment_intents SET owner_user_id = #{ownerUserId} WHERE id = #{id}")
    int assignOwner(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId);

    @Update("UPDATE enrollment_intents SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT LAST_INSERT_ID()")
    Long lastInsertId();

    @Select("""
            SELECT id, lead_id AS leadId, content, follow_type AS followType,
                   next_follow_time AS nextFollowTime, creator_user_id AS creatorUserId,
                   creator_name AS creatorName, create_time AS createTime
            FROM (
              SELECT fr.*, u.nickname AS creator_name
              FROM enrollment_follow_records fr
              LEFT JOIN users u ON u.id = fr.creator_user_id
            ) t
            WHERE lead_id = #{leadId}
            ORDER BY create_time DESC, id DESC
            """)
    List<EnrollmentFollowUpDto> listFollowUps(@Param("leadId") Long leadId);

    @Insert("""
            INSERT INTO enrollment_follow_records
              (lead_id, content, follow_type, next_follow_time, creator_user_id)
            VALUES
              (#{leadId}, #{content}, #{followType}, #{nextFollowTime}, #{creatorUserId})
            """)
    int insertFollowUp(@Param("leadId") Long leadId,
                       @Param("content") String content,
                       @Param("followType") String followType,
                       @Param("nextFollowTime") LocalDateTime nextFollowTime,
                       @Param("creatorUserId") Long creatorUserId);

    @Select("SELECT COUNT(*) FROM enrollment_intents WHERE DATE(create_time) = CURDATE()")
    long countTodayLeads();

    @Select("SELECT COUNT(*) FROM enrollment_intents")
    long countAllLeads();

    @Select("SELECT COUNT(*) FROM enrollment_intents WHERE status = '已报名'")
    long countSignedLeads();

    @Select("""
            SELECT source, COUNT(*) AS count
            FROM enrollment_intents
            GROUP BY source
            ORDER BY count DESC
            """)
    List<EnrollmentSourceStatDto> sourceStats();

    @Select("""
            SELECT ei.owner_user_id AS ownerUserId,
                   COALESCE(u.nickname, '未分配') AS ownerName,
                   COUNT(*) AS signedCount
            FROM enrollment_intents ei
            LEFT JOIN users u ON u.id = ei.owner_user_id
            WHERE ei.status = '已报名'
            GROUP BY ei.owner_user_id, u.nickname
            ORDER BY signedCount DESC
            LIMIT 10
            """)
    List<EnrollmentOwnerPerformanceDto> ownerRanking();

    @Select("""
            SELECT intent_level AS intentLevel, COUNT(*) AS count
            FROM enrollment_intents
            GROUP BY intent_level
            ORDER BY count DESC
            """)
    List<EnrollmentIntentStatDto> intentStats();

    @Select("""
            SELECT status AS stage, COUNT(*) AS count
            FROM enrollment_intents
            GROUP BY status
            ORDER BY FIELD(status, '待跟进', '已联系', '有意向', '已报名', '已放弃', '无效线索'), status
            """)
    List<EnrollmentFunnelStatDto> funnelStats();
}
