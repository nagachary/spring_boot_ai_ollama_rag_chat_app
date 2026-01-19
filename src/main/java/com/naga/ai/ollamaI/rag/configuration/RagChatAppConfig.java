package com.naga.ai.ollamaI.rag.configuration;

import com.naga.ai.ollamaI.rag.embedding.DocumentEmbeddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagChatAppConfig {

    @Value("${document.embedding.file-path}")
    private String docsPath;

    @Bean
    @ConditionalOnProperty(name = "document.embedding.enabled", havingValue = "true")
    ApplicationRunner runner(DocumentEmbeddingService embeddingService) {
        return args -> {
            embeddingService.loadData(docsPath);
        };
    }
}
