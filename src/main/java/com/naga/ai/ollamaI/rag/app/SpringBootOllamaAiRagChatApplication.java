package com.naga.ai.ollamaI.rag.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.naga.*","com.spring.*"})
public class SpringBootOllamaAiRagChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootOllamaAiRagChatApplication.class, args);
	}

}
