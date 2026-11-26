package com.example.rag.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class NoticeLoader {
    public List<Document> load() throws IOException {
        var resource = new ClassPathResource("school-notice.txt");
        String text = resource.getContentAsString(StandardCharsets.UTF_8);
        return splitIntoChunks(text);
    }

    List<Document> splitIntoChunks(String text) {
        String normalizedText = text.replace("\r\n", "\n");
        List<Document> chunks = new ArrayList<>();

        // 빈 줄로 문단을 나눈다. 문단 하나가 청크 하나다.
        for (String paragraph : normalizedText.split("\n\\s*\n")) {
            String[] lines = paragraph.strip().split("\n", 2);
            if (!lines[0].startsWith("## ") || lines.length < 2) {
                continue;
            }

            // 첫 줄은 제목, 나머지는 본문이다.
            String title = lines[0].substring(3).strip();
            String body = lines[1].strip();
            if (title.isBlank() || body.isBlank()) {
                continue;
            }

            String chunkId = "notice-" + (chunks.size() + 1);
            chunks.add(Document.builder()
                    .id(chunkId)
                    .text(title + "\n" + body)
                    .metadata(Map.of("title", title, "chunkId", chunkId))
                    .build());
        }

        if (chunks.isEmpty()) {
            throw new IllegalStateException("안내문 청크가 없습니다.");
        }
        return chunks;
    }
}
