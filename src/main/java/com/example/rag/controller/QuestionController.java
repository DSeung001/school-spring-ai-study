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
    // 생성자 주입: Spring이 만들어 둔 QuestionService를 넣어 줌. 직접 new 하지 않음
    public QuestionController(QuestionService service) { this.service = service; }

    @PostMapping
    // 성공 응답 코드를 기본 200 대신 201로
    @ResponseStatus(HttpStatus.CREATED)
    // @RequestBody: 요청 JSON을 QuestionRequest로 변환. @Valid: 규칙 위반이면 이 메서드를 실행하지 않고 400
    public Question create(@Valid @RequestBody QuestionRequest request) {
        return service.create(request.content());
    }
    @GetMapping
    // List를 반환하면 JSON 배열 [...]로 직렬화
    public List<Question> findAll() { return service.findAll(); }
}
