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
            // 1. Check Document Count
            Integer docCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM vector_store", Integer.class);

            // 2. Simple "Ping" to Ollama
            // We ask a tiny question to verify the model is loaded and responsive
            String response = chatModel.call("Confirm status with one word: 'Ready'.");
            boolean isAiReady = response != null && response.toLowerCase().contains("ready");

            if (!isAiReady) {
                return Health.status("DEGRADED")
                        .withDetail("ollama", "Server responded but model is not ready")
                        .build();
            }

            return Health.up()
                    .withDetail("ollama", "Responsive")
                    .withDetail("vectorStoreCount", docCount)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .withDetail("reason", "Ollama or Database unreachable")
                    .build();
        }
    }
}
