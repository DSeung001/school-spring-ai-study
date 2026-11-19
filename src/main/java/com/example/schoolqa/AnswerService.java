package com.example.schoolqa;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AnswerService {
    private final ChatClient chatClient;
    private final String systemPrompt;

    public AnswerService(ChatClient.Builder builder,
            @Value("${app.system-prompt}") String systemPrompt) {
        this.chatClient = builder.build();
        this.systemPrompt = systemPrompt;
    }
    public String answer(String question) {
        String reply;
        try {
            reply = chatClient.prompt().system(systemPrompt)
                    .user(question).call().content();
        } catch (RuntimeException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI 서버에 연결하지 못했습니다. 모델 실행 상태를 확인해 주세요.");
        }
        if (reply == null || reply.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 답변이 비어 있습니다.");
        }
        return reply.strip();
    }
}
