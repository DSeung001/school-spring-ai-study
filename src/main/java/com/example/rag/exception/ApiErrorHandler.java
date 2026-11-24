package com.example.rag.exception;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

// 모든 Controller에서 던진 예외를 Spring이 여기로 보냄. 반환값은 Controller처럼 JSON이 됨
@RestControllerAdvice
public class ApiErrorHandler {
    // 괄호 안 예외 종류가 나면 이 메서드가 실행됨
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException error) {
        String message = error.getReason() == null ? "요청을 처리하지 못했습니다." : error.getReason();
        return ResponseEntity.status(error.getStatusCode()).body(Map.of("message", message));
    }
    // @Valid 검사 실패 때 Spring이 던지는 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException error) {
        String message = error.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage()).findFirst().orElse("입력값을 확인해 주세요.");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}
