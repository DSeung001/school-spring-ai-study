package com.example.rag;

import com.example.rag.dto.GeneratedAnswer;
import com.example.rag.service.AnswerService;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 임베딩 저장소와 채팅 모델은 테스트 대역. 반드시 전용 *_test DB로 실행.
@SpringBootTest(properties = "app.rag.index-on-startup=false")
@AutoConfigureMockMvc
class NoticeSearchApiTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired AnswerService answerService;
    @MockitoBean SimpleVectorStore store;
    @MockitoBean ChatModel chatModel;

    private final Document library = Document.builder().id("notice-1").text("도서관\n세 권까지 빌림")
            .metadata(Map.of("title", "도서관", "chunkId", "notice-1")).score(0.83).build();

    @BeforeEach
    void prepare() {
        String database = jdbc.queryForObject("SELECT DATABASE()", String.class);
        assertTrue(database.endsWith("_test"), "삭제 검증은 *_test DB에서만 실행합니다.");
        jdbc.update("DELETE FROM questions");
    }

    private org.springframework.test.web.servlet.ResultActions search(String json) throws Exception {
        return mvc.perform(post("/api/search").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private int questionCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM questions", Integer.class)
                + jdbc.queryForObject("SELECT COUNT(*) FROM question_sources", Integer.class);
    }

    @Test
    void returnsSourcesWithoutChatModelOrDatabaseWrite() throws Exception {
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(library));
        search("{\"content\":\"  책은 몇 권 빌려요?  \"}").andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("도서관"))
                .andExpect(jsonPath("$[0].chunkId").value("notice-1"))
                .andExpect(jsonPath("$[0].text").value("도서관\n세 권까지 빌림"))
                .andExpect(jsonPath("$[0].score").value(0.83));
        var request = ArgumentCaptor.forClass(SearchRequest.class);
        verify(store).similaritySearch(request.capture());
        assertEquals("책은 몇 권 빌려요?", request.getValue().getQuery());
        assertEquals(3, request.getValue().getTopK());
        assertEquals(0.50, request.getValue().getSimilarityThreshold());
        verifyNoInteractions(chatModel);
        assertEquals(0, questionCount());
    }

    @Test
    void emptyResultInvalidInputAndSearchFailure() throws Exception {
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        search("{\"content\":\"주식 가격\"}").andExpect(status().isOk()).andExpect(content().json("[]"));
        search("{\"content\":\"  \"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("질문을 입력해 주세요."));
        search("{\"content\":\"" + "가".repeat(201) + "\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("질문은 200자 이내로 입력해 주세요."));
        search("{}").andExpect(status().isBadRequest());
        verify(store, times(1)).similaritySearch(any(SearchRequest.class));

        when(store.similaritySearch(any(SearchRequest.class))).thenThrow(new RuntimeException("Ollama 중지"));
        search("{\"content\":\"도서관\"}").andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("안내문 검색에 실패했습니다. 임베딩 모델을 확인해 주세요."));
        verifyNoInteractions(chatModel);
        assertEquals(0, questionCount());
    }

    @Test
    void answerServiceUsesTheSameSearch() {
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        GeneratedAnswer withheld = answerService.answer("사서 선생님 성함은?");
        assertEquals("안내문에서 근거를 찾지 못했습니다. 담당 선생님께 확인해 주세요.", withheld.answer());
        assertTrue(withheld.sources().isEmpty());
        verifyNoInteractions(chatModel);

        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(library));
        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());
        when(chatModel.call(any(Prompt.class))).thenReturn(
                new ChatResponse(List.of(new Generation(new AssistantMessage(" 세 권입니다. [notice-1] ")))));
        GeneratedAnswer generated = answerService.answer("책은 몇 권?");
        assertEquals("세 권입니다. [notice-1]", generated.answer());
        assertEquals("notice-1", generated.sources().getFirst().chunkId());
        var requests = ArgumentCaptor.forClass(SearchRequest.class);
        verify(store, times(2)).similaritySearch(requests.capture());
        requests.getAllValues().forEach(r -> assertEquals(3, r.getTopK()));
        var prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(prompt.capture());
        assertTrue(prompt.getValue().getContents().contains("[notice-1] 도서관\n세 권까지 빌림"));
    }
}
