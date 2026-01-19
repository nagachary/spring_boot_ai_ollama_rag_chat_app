package com.naga.ai.ollamaI.rag.health;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class RagChatAppHealthIndicator implements HealthIndicator {
    private final JdbcTemplate jdbcTemplate;
    private final OllamaChatModel chatModel;

    public RagChatAppHealthIndicator(JdbcTemplate jdbcTemplate, OllamaChatModel chatModel) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatModel = chatModel;
    }

    @Override
    public Health health() {
        try {
            Integer docCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vector_store", Integer.class);
            String response = chatModel.call("Confirm status with one word: 'Ready'.");

            return Health.up()
                    .withDetail("ollama", "Responsive")
                    .withDetail("modelStatus", response)
                    .withDetail("vectorStoreCount", docCount)
                    .build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
