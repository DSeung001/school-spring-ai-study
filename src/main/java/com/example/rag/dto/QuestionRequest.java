package com.example.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionRequest(
        @NotBlank(message = "질문을 입력해 주세요.")
        @Size(max = 200, message = "질문은 200자 이내로 입력해 주세요.")
        String content) {
}
