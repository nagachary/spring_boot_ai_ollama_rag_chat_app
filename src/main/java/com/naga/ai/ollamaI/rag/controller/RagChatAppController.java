package com.naga.ai.ollamaI.rag.controller;

import com.naga.ai.ollamaI.rag.embedding.DocumentEmbeddingService;
import com.naga.ai.ollamaI.rag.exception.DocumentEmbeddingException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/rag_chat")
public class RagChatAppController {
    Logger logger = LoggerFactory.getLogger(RagChatAppController.class);

    private final ChatClient chatClient;
    private final DocumentEmbeddingService embeddingService;

    @Value("${document.embedding.file-path}")
    private String docsPath;

    public RagChatAppController(ChatClient.Builder builder, DocumentEmbeddingService embeddingService, VectorStore vectorStore) {
        this.embeddingService = embeddingService;
        this.chatClient = builder
                .defaultAdvisors(QuestionAnswerAdvisor
                        .builder(vectorStore).build()).build();
    }

    @PostMapping("/healthy_tips")
    public String chat(@RequestBody String message) {
        logger.info("chat");
        /** var chatClient = ChatClient
         .builder(ollamaChatModel)
         .defaultAdvisors(QuestionAnswerAdvisor
         .builder(vectorStore).build())
         .build(); **/

        return chatClient
                .prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, "12345"))
                .user(message)
                .call()
                .content();
    }

    /**
     * Manual trigger for re-indexing documents.
     * Useful if you add files while the app is running.
     */
    @PostMapping("/reindex")
    public ResponseEntity<String> reindex() throws DocumentEmbeddingException {
        logger.info("reindex");
        embeddingService.loadData(docsPath);
        return ResponseEntity.ok("Re-indexing triggered. Check logs for MD5 skip/update status.");
    }
}
