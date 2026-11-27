package com.example.rag.repository;

import java.util.List;
import com.example.rag.dto.QuestionSourceRow;
import com.example.rag.model.Question;
import org.apache.ibatis.annotations.*;

// MyBatis가 이 인터페이스의 구현 객체를 만듦. #{...}는 값을 안전하게 바인딩함.
@Mapper
public interface QuestionMapper {
    @Insert("INSERT INTO questions (content, answer) VALUES (#{content}, #{answer})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Question question);

    @Select("SELECT id, content, answer FROM questions ORDER BY id")
    List<Question> findAll();

    @Select("SELECT id, content, answer FROM questions WHERE id = #{id}")
    Question findById(@Param("id") long id);

    @Update("UPDATE questions SET content = #{content}, answer = #{answer} WHERE id = #{id}")
    int update(Question question);

    @Delete("DELETE FROM questions WHERE id = #{id}")
    int delete(@Param("id") long id);

    // 질문 하나에 근거가 여러 개면 질문 컬럼이 반복된 여러 행이 나옴. 근거가 없으면 근거 컬럼이 NULL인 한 행.
    @Select("""
            SELECT q.id AS question_id, q.content, q.answer,
                   s.source_order, s.title, s.chunk_id, s.source_text, s.score
            FROM questions q
            LEFT JOIN question_sources s ON s.question_id = q.id
            ORDER BY q.id, s.source_order
            """)
    // 컬럼을 QuestionSourceRow 생성자 인자에 순서대로 연결. 아래 두 메서드도 id로 같은 연결을 재사용함.
    @Results(id = "questionSourceRow")
    @ConstructorArgs({
        @Arg(column = "question_id", javaType = long.class),
        @Arg(column = "content", javaType = String.class),
        @Arg(column = "answer", javaType = String.class),
        @Arg(column = "source_order", javaType = Integer.class),
        @Arg(column = "title", javaType = String.class),
        @Arg(column = "chunk_id", javaType = String.class),
        @Arg(column = "source_text", javaType = String.class),
        @Arg(column = "score", javaType = Double.class)
    })
    List<QuestionSourceRow> findAllWithSources();

    // 검색 조건은 질문·답변 컬럼에만 걸어 일치한 질문의 근거는 모두 함께 조회됨.
    @Select("""
            SELECT q.id AS question_id, q.content, q.answer,
                   s.source_order, s.title, s.chunk_id, s.source_text, s.score
            FROM questions q
            LEFT JOIN question_sources s ON s.question_id = q.id
            WHERE q.content LIKE #{pattern} OR q.answer LIKE #{pattern}
            ORDER BY q.id, s.source_order
            """)
    @ResultMap("questionSourceRow")
    List<QuestionSourceRow> searchWithSources(@Param("pattern") String pattern);

    @Select("""
            SELECT q.id AS question_id, q.content, q.answer,
                   s.source_order, s.title, s.chunk_id, s.source_text, s.score
            FROM questions q
            LEFT JOIN question_sources s ON s.question_id = q.id
            WHERE q.id = #{id}
            ORDER BY s.source_order
            """)
    @ResultMap("questionSourceRow")
    List<QuestionSourceRow> findByIdWithSources(@Param("id") long id);
}
