package com.example.rag.config;

import com.example.rag.service.NoticeLoader;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfig {
    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
    @Bean
    @ConditionalOnProperty(name = "app.rag.index-on-startup", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner indexNotice(SimpleVectorStore store, NoticeLoader loader) {
        return args -> {
            var chunks = loader.load();
            store.add(chunks);
            System.out.println("안내문 인덱싱 완료: " + chunks.size() + "개 청크");
        };
    }
}
