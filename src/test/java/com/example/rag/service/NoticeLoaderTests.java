package com.example.rag.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import static org.junit.jupiter.api.Assertions.*;

class NoticeLoaderTests {
    private final NoticeLoader loader = new NoticeLoader();

    @Test
    void loadsFourNoticeParagraphsWithStableSources() throws Exception {
        List<Document> chunks = loader.load();
        assertEquals(List.of("도서관", "동아리", "분실물", "실습실"),
                chunks.stream().map(chunk -> chunk.getMetadata().get("title")).toList());
        for (int index = 0; index < chunks.size(); index++) {
            Document chunk = chunks.get(index);
            String chunkId = "notice-" + (index + 1);
            assertEquals(chunkId, chunk.getId());
            assertEquals(chunkId, chunk.getMetadata().get("chunkId"));
            assertTrue(chunk.getText().startsWith(chunk.getMetadata().get("title") + "\n"));
        }
        assertTrue(chunks.getFirst().getText().contains("세 권"));
    }

    @Test
    void handlesWindowsLineEndingsAndRepeatedBlankLines() {
        String text = "# 안내문\n\n\n## 도서관\n첫 줄\n둘째 줄\n \n\n## 동아리\n신청 안내\n";
        List<Document> unixChunks = loader.splitIntoChunks(text);
        List<Document> windowsChunks = loader.splitIntoChunks(text.replace("\n", "\r\n"));
        assertEquals(2, unixChunks.size());
        assertEquals("도서관\n첫 줄\n둘째 줄", unixChunks.getFirst().getText());
        assertEquals(unixChunks.stream().map(Document::getText).toList(),
                windowsChunks.stream().map(Document::getText).toList());
        assertEquals(unixChunks.stream().map(Document::getMetadata).toList(),
                windowsChunks.stream().map(Document::getMetadata).toList());
    }

    @Test
    void skipsHeadingsWithoutContentAndNumbersOnlyValidChunks() {
        List<Document> chunks = loader.splitIntoChunks(
                "# 전체 제목\n\n## 제목만 있음\n\n##   \n제목 없음\n\n## 빈 본문\n  \n\n## 도서관\n대출 안내");
        assertEquals(1, chunks.size());
        assertEquals("notice-1", chunks.getFirst().getId());
        assertEquals("도서관\n대출 안내", chunks.getFirst().getText());
    }

    @Test
    void keepsLongParagraphAndEmojiInOneChunk() {
        String body = "대출 안내 📚 ".repeat(40);
        List<Document> chunks = loader.splitIntoChunks("## 도서관\n" + body);
        assertEquals(1, chunks.size());
        assertEquals("도서관\n" + body.strip(), chunks.getFirst().getText());
    }

    @Test
    void rejectsDocumentsWithoutUsableChunks() {
        for (String text : List.of("", "\n\n", "# 안내문", "## 제목만 있음", "## 도서관\n  ")) {
            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> loader.splitIntoChunks(text));
            assertEquals("안내문 청크가 없습니다.", error.getMessage());
        }
    }
}
