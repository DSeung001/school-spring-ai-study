package com.example.rag.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// DB·Ollama 없이 app.rag.top-k 설정만 확인함.
class NoticeSearchServiceTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(SimpleVectorStore.class, () -> mock(SimpleVectorStore.class))
            .withBean(NoticeSearchService.class);

    private int searchedTopK(org.springframework.context.ApplicationContext context) {
        SimpleVectorStore store = context.getBean(SimpleVectorStore.class);
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        context.getBean(NoticeSearchService.class).search("도서관");
        var request = ArgumentCaptor.forClass(SearchRequest.class);
        verify(store).similaritySearch(request.capture());
        return request.getValue().getTopK();
    }

    @Test
    void usesThreeWhenTopKIsMissing() {
        runner.run(context -> assertEquals(3, searchedTopK(context)));
    }

    @Test
    void usesConfiguredTopK() {
        runner.withPropertyValues("app.rag.top-k=1")
                .run(context -> assertEquals(1, searchedTopK(context)));
    }

    @Test
    void failsAtStartupWhenTopKIsLessThanOne() {
        for (String value : List.of("0", "-1")) {
            runner.withPropertyValues("app.rag.top-k=" + value).run(context -> {
                assertNotNull(context.getStartupFailure());
                Throwable cause = context.getStartupFailure();
                while (cause.getCause() != null) cause = cause.getCause();
                assertEquals("app.rag.top-k는 1 이상이어야 합니다.", cause.getMessage());
            });
        }
        runner.withPropertyValues("app.rag.top-k=abc")
                .run(context -> assertNotNull(context.getStartupFailure()));
    }
}
