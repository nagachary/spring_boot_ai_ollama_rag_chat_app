package com.naga.ai.ollamaI.rag.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class RagChatAppController {
    Logger logger = LoggerFactory.getLogger(RagChatAppController.class);
    private final OllamaChatModel ollamaChatModel;
    private final VectorStore vectorStore;

    public RagChatAppController(OllamaChatModel ollamaChatModel, VectorStore vectorStore) {
        this.ollamaChatModel = ollamaChatModel;
        this.vectorStore = vectorStore;
    }

    @PostMapping("/rag_chat")
    public String chat(@RequestBody String message) {
        logger.info("chat");
        var chatClient = ChatClient
                .builder(ollamaChatModel)
                .defaultAdvisors(QuestionAnswerAdvisor
                        .builder(vectorStore).build())
                .build();

        return chatClient
                .prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, "12345"))
                .user(message)
                .call()
                .content();
    }
}
