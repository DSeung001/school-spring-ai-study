package com.example.rag;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RagFallbackTest {
    @Test void emptySearchSkipsGeneration() {
        var builder = mock(ChatClient.Builder.class);
        var client = mock(ChatClient.class);
        var store = mock(SimpleVectorStore.class);
        when(builder.build()).thenReturn(client);
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        var answer = new AnswerService(builder, store, "학교 안내", 0.5).answer("없는 질문");
        assertTrue(answer.sources().isEmpty());
        assertTrue(answer.answer().contains("담당 선생님"));
        verifyNoInteractions(client);
    }
}
