package com.example.rag;

import com.example.rag.model.Question;
import com.example.rag.repository.QuestionMapper;
import com.example.rag.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.rag.service.AnswerService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.*;
import com.example.rag.dto.GeneratedAnswer;
import com.example.rag.model.Source;
import com.example.rag.repository.SourceMapper;
import java.util.List;

// 반드시 전용 *_test DB로 실행. H2가 아닌 MySQL의 키 생성·FK·롤백을 검증함.
@SpringBootTest(properties = "app.rag.index-on-startup=false")
@AutoConfigureMockMvc
class QuestionPersistenceTests {
    @Autowired QuestionService service;
    @Autowired QuestionMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired TransactionTemplate transaction;
    @Autowired MockMvc mvc;
    @MockitoBean AnswerService answerService;
    @Autowired SourceMapper sourceMapper;

    @BeforeEach
    void prepare() {
        String database = jdbc.queryForObject("SELECT DATABASE()", String.class);
        assertTrue(database.endsWith("_test"), "삭제 검증은 *_test DB에서만 실행합니다.");
        jdbc.update("DELETE FROM questions");
        when(answerService.answer(anyString())).thenReturn(new GeneratedAnswer("안내 답변", List.of()));
    }

    @Test
    void crudAndHttpErrors() throws Exception {
        mvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"도서관 질문\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.content").value("도서관 질문"));
        long id = mapper.findAll().getFirst().getId();
        assertTrue(id > 0);
        mvc.perform(get("/api/questions")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(put("/api/questions/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"수정한 질문\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").value("수정한 질문"));
        // 같은 값으로 수정해도 없는 질문 404로 처리하지 않음.
        assertEquals(id, service.update(id, "수정한 질문").getId());
        mvc.perform(delete("/api/questions/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/questions/{id}", id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."));
        mvc.perform(put("/api/questions/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"없는 질문 수정\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        assertTrue(mapper.findAll().isEmpty());
    }

    @Test
    void koreanAndSqlBindingAndIdOrdering() {
        String content = "한글 ' OR 1=1 -- 질문";
        Question first = service.create(content);
        Question second = service.create("두 번째 질문");
        assertEquals(content, mapper.findById(first.getId()).getContent());
        assertEquals(java.util.List.of(first.getId(), second.getId()), service.findAll().stream().map(Question::getId).toList());
        assertTrue(second.getId() > first.getId());
    }

    @Test
    void mybatisAndJdbcUseTheSameTransaction() {
        assertThrows(DataAccessException.class, () -> transaction.execute(status -> {
            mapper.insert(new Question(0, "롤백될 질문", "답변"));
            jdbc.update("INSERT INTO questions (content, answer) VALUES (NULL, '실패')");
            return null;
        }));
        assertTrue(mapper.findAll().isEmpty());
    }

    @Test
    void modelFailureLeavesExistingData() {
        Question existing = service.create("원래 질문");
        when(answerService.answer(anyString())).thenThrow(
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "모델 연결 실패"));
        assertThrows(ResponseStatusException.class, () -> service.create("실패할 등록"));
        assertThrows(ResponseStatusException.class, () -> service.update(existing.getId(), "실패할 수정"));
        assertEquals(1, mapper.findAll().size());
        assertEquals("원래 질문", mapper.findById(existing.getId()).getContent());
    }

    @Test
    void evidenceIsStoredInOrderReplacedAndDeleted() {
        var sources = List.of(new Source("도서관", "notice-1", "평일 개방", 0.9),
                new Source("동아리", "notice-2", "수요일 활동", 0.8));
        when(answerService.answer(anyString())).thenReturn(new GeneratedAnswer("근거 답변", sources));
        Question existing = service.create("근거 질문");
        assertEquals(sources, service.findAll().getFirst().getSources());
        when(answerService.answer(anyString())).thenReturn(new GeneratedAnswer("근거 없음", List.of()));
        service.update(existing.getId(), "근거 없는 질문");
        assertTrue(service.findAll().getFirst().getSources().isEmpty());
        when(answerService.answer(anyString())).thenReturn(new GeneratedAnswer("근거 답변", sources));
        service.update(existing.getId(), "다시 근거 질문");
        service.delete(existing.getId());
        assertTrue(sourceMapper.findByQuestionId(existing.getId()).isEmpty());
    }

    @Test
    void evidenceFailureRollsBackQuestionAndOldEvidence() throws Exception {
        var original = List.of(new Source("원래 제목", "notice-1", "원래 근거", 0.9));
        when(answerService.answer(anyString())).thenReturn(new GeneratedAnswer("원래 답변", original));
        Question existing = service.create("원래 질문");
        // 첫 근거는 저장되지만 두 번째 근거의 NOT NULL 위반으로 전체를 롤백해야 함.
        var invalid = List.of(new Source("새 제목", "notice-2", "새 근거", 0.8),
                new Source(null, "notice-3", "실패 근거", 0.7));
        when(answerService.answer(anyString())).thenReturn(new GeneratedAnswer("바뀐 답변", invalid));
        assertThrows(DataAccessException.class, () -> service.update(existing.getId(), "바뀐 질문"));
        assertEquals("원래 질문", mapper.findById(existing.getId()).getContent());
        assertEquals("원래 답변", mapper.findById(existing.getId()).getAnswer());
        assertEquals(original, sourceMapper.findByQuestionId(existing.getId()));
        mvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"실패할 새 질문\"}"))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.message").exists());
        assertEquals(1, mapper.findAll().size());
    }
}
