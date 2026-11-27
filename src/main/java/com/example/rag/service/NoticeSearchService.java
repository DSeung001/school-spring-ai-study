package com.example.rag.service;

import com.example.rag.model.Source;

import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// 안내문 검색만 담당함. 답변 생성(AnswerService)과 검색 전용 API가 함께 사용함.
@Service
public class NoticeSearchService {
    private final SimpleVectorStore store;
    private final int topK;
    private final double threshold;

    // ":3", ":0.50"은 yaml에 값이 없을 때 쓸 기본값
    public NoticeSearchService(SimpleVectorStore store,
            @Value("${app.rag.top-k:3}") int topK,
            @Value("${app.rag.similarity-threshold:0.50}") double threshold) {
        if (topK < 1) {
            // 생성자에서 실패하면 서버가 시작되지 않으므로 잘못된 설정을 바로 알 수 있음.
            throw new IllegalArgumentException("app.rag.top-k는 1 이상이어야 합니다.");
        }
        this.store = store;
        this.topK = topK;
        this.threshold = threshold;
    }

    public List<Source> search(String query) {
        List<Document> found;
        try {
            found = store.similaritySearch(SearchRequest.builder().query(query)
                    .topK(topK).similarityThreshold(threshold).build());
        } catch (RuntimeException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "안내문 검색에 실패했습니다. 임베딩 모델을 확인해 주세요.");
        }
        return found.stream().map(d -> new Source(
                d.getMetadata().get("title").toString(),
                d.getMetadata().get("chunkId").toString(), d.getText(), d.getScore())).toList();
    }
}
