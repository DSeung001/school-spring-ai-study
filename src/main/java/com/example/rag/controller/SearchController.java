package com.example.rag.controller;

import com.example.rag.dto.QuestionRequest;
import com.example.rag.model.Source;
import com.example.rag.service.NoticeSearchService;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

// 답변 생성과 DB 저장 없이 안내문 검색 결과만 돌려줌.
@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final NoticeSearchService service;
    public SearchController(NoticeSearchService service) { this.service = service; }

    @PostMapping
    public List<Source> search(@Valid @RequestBody QuestionRequest request) {
        return service.search(request.content().strip());
    }
}
