package com.example.rag.repository;

import java.util.List;
import com.example.rag.model.Comment;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CommentMapper {
    @Insert("INSERT INTO comments (question_id, content) VALUES (#{questionId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    // created_at 칼럼은 MyBatis 설정 없이도 createdAt 필드에 담기도록 별칭을 붙임.
    @Select("""
            SELECT id, question_id AS questionId, content, created_at AS createdAt
            FROM comments WHERE id = #{id}
            """)
    Comment findById(@Param("id") long id);

    @Select("""
            SELECT id, question_id AS questionId, content, created_at AS createdAt
            FROM comments WHERE question_id = #{questionId} ORDER BY id
            """)
    List<Comment> findByQuestionId(@Param("questionId") long questionId);

    @Delete("DELETE FROM comments WHERE id = #{id}")
    int delete(@Param("id") long id);
}
