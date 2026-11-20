package com.example.rag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpeechRequest(
        @NotBlank(message = "읽을 내용을 입력해 주세요.")
        @Size(max = 1000, message = "음성으로 읽을 내용은 1000자 이내여야 합니다.")
        String text) {
}
