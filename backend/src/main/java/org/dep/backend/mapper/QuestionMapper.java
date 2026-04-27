package org.dep.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dep.backend.mapper.projection.WrongQuestionRow;
import org.dep.backend.model.Question;

import java.util.List;

@Mapper
public interface QuestionMapper {

    @Select("""
            SELECT id,
                   content,
                   option_a AS optionA,
                   option_b AS optionB,
                   option_c AS optionC,
                   option_d AS optionD,
                   answer,
                   explanation
            FROM questions
            WHERE exam_type = #{examType}
            ORDER BY id
            """)
    List<Question> findAllByExamType(@Param("examType") String examType);

    @Select("""
            SELECT id,
                   content,
                   option_a AS optionA,
                   option_b AS optionB,
                   option_c AS optionC,
                   option_d AS optionD,
                   answer,
                   explanation
            FROM questions
            ORDER BY id
            """)
    List<Question> findAll();

    @Select("""
            SELECT id,
                   content,
                   option_a AS optionA,
                   option_b AS optionB,
                   option_c AS optionC,
                   option_d AS optionD,
                   answer,
                   explanation
            FROM questions
            WHERE exam_type = #{examType}
            ORDER BY RAND()
            LIMIT #{count}
            """)
    List<Question> findRandomByExamType(@Param("examType") String examType, @Param("count") int count);

    @Select("""
            SELECT id,
                   content,
                   option_a AS optionA,
                   option_b AS optionB,
                   option_c AS optionC,
                   option_d AS optionD,
                   answer,
                   explanation
            FROM questions
            ORDER BY RAND()
            LIMIT #{count}
            """)
    List<Question> findRandom(@Param("count") int count);

    @Select("""
            SELECT id,
                   content,
                   option_a AS optionA,
                   option_b AS optionB,
                   option_c AS optionC,
                   option_d AS optionD,
                   answer,
                   explanation
            FROM questions
            WHERE id = #{questionId}
            """)
    Question findQuestionById(@Param("questionId") Long questionId);

    @Select("""
            SELECT
              w.id AS wrongId,
              w.user_id AS userId,
              w.create_time AS createTime,
              q.id AS questionId,
              q.content,
              q.option_a AS optionA,
              q.option_b AS optionB,
              q.option_c AS optionC,
              q.option_d AS optionD,
              q.answer,
              q.explanation
            FROM wrong_questions w
            JOIN questions q ON q.id = w.question_id
            WHERE w.user_id = #{userId}
            ORDER BY w.create_time DESC
            """)
    List<WrongQuestionRow> findWrongQuestionsByUserId(@Param("userId") Long userId);

    @Insert("""
            INSERT IGNORE INTO wrong_questions (user_id, question_id, create_time)
            VALUES (#{userId}, #{questionId}, NOW())
            """)
    int insertWrongQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);

    @Select("""
            SELECT
              w.id AS wrongId,
              w.user_id AS userId,
              w.create_time AS createTime,
              q.id AS questionId,
              q.content,
              q.option_a AS optionA,
              q.option_b AS optionB,
              q.option_c AS optionC,
              q.option_d AS optionD,
              q.answer,
              q.explanation
            FROM wrong_questions w
            JOIN questions q ON q.id = w.question_id
            WHERE w.user_id = #{userId}
              AND w.question_id = #{questionId}
            """)
    WrongQuestionRow findWrongQuestionByUserAndQuestion(@Param("userId") Long userId,
                                                        @Param("questionId") Long questionId);

    @Delete("DELETE FROM wrong_questions WHERE id = #{id} AND user_id = #{userId}")
    int deleteWrongQuestion(@Param("userId") Long userId, @Param("id") Long id);

    @Delete("DELETE FROM wrong_questions WHERE user_id = #{userId}")
    int clearWrongQuestions(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'questions'
              AND COLUMN_NAME = 'exam_type'
            """)
    Integer countExamTypeColumn();

    @Insert("ALTER TABLE questions ADD COLUMN exam_type VARCHAR(20) NOT NULL DEFAULT '科目一'")
    int addExamTypeColumn();
}
