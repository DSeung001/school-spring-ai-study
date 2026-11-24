package com.example.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 요청 JSON의 "content" 키가 같은 이름의 content에 들어감(Jackson 역직렬화)
public record QuestionRequest(
        // 아래 규칙은 Controller 매개변수에 @Valid가 있을 때 Spring이 검사함
        @NotBlank(message = "질문을 입력해 주세요.")
        @Size(max = 200, message = "질문은 200자 이내로 입력해 주세요.")
        String content) {
}
