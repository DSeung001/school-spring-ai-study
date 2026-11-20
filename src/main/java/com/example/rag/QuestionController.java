package com.example.rag;

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
    @GetMapping
    public List<Question> findAll() { return service.findAll(); }

    @PutMapping("/{id}")
    public Question update(@PathVariable long id, @Valid @RequestBody QuestionRequest request) {
        return service.update(id, request.content());
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) { service.delete(id); }
}
