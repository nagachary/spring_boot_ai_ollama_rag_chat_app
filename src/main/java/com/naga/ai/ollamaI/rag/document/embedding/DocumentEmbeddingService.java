package com.naga.ai.ollamaI.rag.document.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentEmbeddingService implements CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(DocumentEmbeddingService.class);
    @Value("classpath:/documents/Ten_Tips_Healthy_Lifestyle.pdf")
    private Resource resource;

    private final VectorStore vectorStore;

    public DocumentEmbeddingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("run");
        //Read the pdf
        TikaDocumentReader documentReader = new TikaDocumentReader(resource);
        //Split the content into chunks
        TextSplitter textSplitter = new TokenTextSplitter(400, 200, 20, 50, true);
        List<Document> documents = textSplitter.split(documentReader.read());
        //Store the data into vector database
        vectorStore.accept(documents);
    }
}
