package com.example.rag.controller;

import com.example.rag.dto.QuestionRequest;
import com.example.rag.model.Question;
import com.example.rag.service.QuestionService;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService service;
    public QuestionController(QuestionService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Question create(@Valid @RequestBody QuestionRequest request) {
        return service.create(request.content());
    }
    // keyword가 없거나 비어 있으면 전체 목록. 예: GET /api/questions?keyword=도서관
    @GetMapping
    public List<Question> findAll(@RequestParam(required = false) String keyword) {
        return service.search(keyword);
    }
    @GetMapping("/{id}")
    public Question findById(@PathVariable long id) { return service.findById(id); }

    @PutMapping("/{id}")
    public Question update(@PathVariable long id, @Valid @RequestBody QuestionRequest request) {
        return service.update(id, request.content());
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) { service.delete(id); }
}
