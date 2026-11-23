package com.example.rag.repository;

import java.util.List;
import com.example.rag.model.Source;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SourceMapper {
    @Insert("""
            INSERT INTO question_sources
                (question_id, source_order, title, chunk_id, source_text, score)
            VALUES (#{questionId}, #{order}, #{source.title}, #{source.chunkId}, #{source.text}, #{source.score})
            """)
    int insert(@Param("questionId") long questionId, @Param("order") int order,
            @Param("source") Source source);

    @Select("""
            SELECT title, chunk_id, source_text, score FROM question_sources
            WHERE question_id = #{questionId} ORDER BY source_order
            """)
    @ConstructorArgs({
        @Arg(column = "title", javaType = String.class),
        @Arg(column = "chunk_id", javaType = String.class),
        @Arg(column = "source_text", javaType = String.class),
        @Arg(column = "score", javaType = double.class)
    })
    List<Source> findByQuestionId(@Param("questionId") long questionId);

    @Delete("DELETE FROM question_sources WHERE question_id = #{questionId}")
    int deleteByQuestionId(@Param("questionId") long questionId);
}
