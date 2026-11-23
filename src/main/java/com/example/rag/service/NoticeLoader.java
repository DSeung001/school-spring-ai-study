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
        String raw = resource.getContentAsString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        List<Document> chunks = new ArrayList<>();
        int number = 1;
        for (String paragraph : raw.split("\n\s*\n")) {
            if (!paragraph.startsWith("## ")) continue;
            int newline = paragraph.indexOf('\n');
            if (newline < 0) continue;
            String title = paragraph.substring(3, newline).strip();
            String body = paragraph.substring(newline + 1).strip();
            for (int start = 0; start < body.length(); start += 170) {
                int end = Math.min(start + 200, body.length());
                String chunkId = "notice-" + number++;
                chunks.add(Document.builder().id(chunkId)
                        .text(title + "\n" + body.substring(start, end))
                        .metadata(Map.of("title", title, "chunkId", chunkId)).build());
                if (end == body.length()) break;
            }
        }
        if (chunks.isEmpty()) throw new IllegalStateException("안내문 청크가 없습니다.");
        return chunks;
    }
}
