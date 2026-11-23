package com.example.rag.repository;

import java.util.List;
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
}
