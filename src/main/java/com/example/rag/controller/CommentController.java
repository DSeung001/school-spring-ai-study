package com.example.rag.controller;

import java.util.List;
import com.example.rag.dto.CommentRequest;
import com.example.rag.model.Comment;
import com.example.rag.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {
    private final CommentService service;
    public CommentController(CommentService service) { this.service = service; }

    // 댓글은 질문에 속하므로 질문 번호를 주소에 넣음. 예: POST /api/questions/1/comments
    @PostMapping("/questions/{questionId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment create(@PathVariable long questionId, @Valid @RequestBody CommentRequest request) {
        return service.create(questionId, request.content());
    }

    @GetMapping("/questions/{questionId}/comments")
    public List<Comment> findByQuestionId(@PathVariable long questionId) {
        return service.findByQuestionId(questionId);
    }

    // 추가 과제: 댓글 번호만으로 삭제. 예: DELETE /api/comments/3
    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) { service.delete(id); }
}
