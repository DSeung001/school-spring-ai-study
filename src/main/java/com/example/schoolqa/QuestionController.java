package com.example.schoolqa;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    @GetMapping("/sample")
    public Question sample() {
        return new Question(1L, "도서관은 언제 열어요?", "안내문을 준비 중입니다.");
    }
}
