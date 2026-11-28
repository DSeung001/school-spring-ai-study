package com.example.rag;

import com.example.rag.dto.GeneratedAnswer;
import com.example.rag.model.Question;
import com.example.rag.service.AnswerService;
import com.example.rag.service.QuestionService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 반드시 전용 *_test DB로 실행. 5일차 익명 댓글 과제의 최종 답안을 실제 MySQL로 검증함.
@SpringBootTest(properties = "app.rag.index-on-startup=false")
@AutoConfigureMockMvc
class CommentApiTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired QuestionService questionService;
    @MockitoBean AnswerService answerService;

    @BeforeEach
    void prepare() {
        String database = jdbc.queryForObject("SELECT DATABASE()", String.class);
        assertTrue(database.endsWith("_test"), "삭제 검증은 *_test DB에서만 실행합니다.");
        jdbc.update("DELETE FROM questions");
        when(answerService.answer(anyString())).thenReturn(new GeneratedAnswer("안내 답변", List.of()));
    }

    private void postComment(long questionId, String content) throws Exception {
        mvc.perform(post("/api/questions/{id}/comments", questionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createAndListCommentsPerQuestion() throws Exception {
        Question first = questionService.create("도서관 질문");
        Question second = questionService.create("동아리 질문");
        mvc.perform(get("/api/questions/{id}/comments", first.getId()))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(post("/api/questions/{id}/comments", first.getId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"  저도 궁금해요  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.questionId").value(first.getId()))
                .andExpect(jsonPath("$.content").value("저도 궁금해요"))
                // LocalDateTime은 2026-11-30T10:15:00 형식 문자열로 내려감
                .andExpect(jsonPath("$.createdAt", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")));
        postComment(first.getId(), "한글 ' OR 1=1 -- 댓글");
        postComment(second.getId(), "다른 질문 댓글");

        mvc.perform(get("/api/questions/{id}/comments", first.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].content", contains("저도 궁금해요", "한글 ' OR 1=1 -- 댓글")));
        mvc.perform(get("/api/questions/{id}/comments", second.getId()))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void validationAndMissingQuestion() throws Exception {
        Question question = questionService.create("질문");
        for (String body : new String[] {"{\"content\":\"   \"}", "{}",
                "{\"content\":\"" + "가".repeat(101) + "\"}"}) {
            mvc.perform(post("/api/questions/{id}/comments", question.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        }
        postComment(question.getId(), "가".repeat(100));
        long missing = question.getId() + 1000;
        mvc.perform(post("/api/questions/{id}/comments", missing)
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"없는 질문\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."));
        mvc.perform(get("/api/questions/{id}/comments", missing))
                .andExpect(status().isNotFound());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM comments", Integer.class));
    }

    @Test
    void deleteCommentAndCascadeWithQuestion() throws Exception {
        Question question = questionService.create("질문");
        postComment(question.getId(), "지울 댓글");
        postComment(question.getId(), "남을 댓글");
        long id = jdbc.queryForObject("SELECT MIN(id) FROM comments", Long.class);
        mvc.perform(delete("/api/comments/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/comments/{id}", id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다."));
        mvc.perform(get("/api/questions/{id}/comments", question.getId()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content").value("남을 댓글"));
        // 질문을 지우면 FK ON DELETE CASCADE로 댓글도 함께 삭제됨
        mvc.perform(delete("/api/questions/{id}", question.getId())).andExpect(status().isNoContent());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM comments", Integer.class));
    }
}
