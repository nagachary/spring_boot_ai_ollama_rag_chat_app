package com.naga.ai.ollamaI.rag.embedding;

import com.naga.ai.ollamaI.rag.exception.DocumentEmbeddingErrorResponse;
import com.naga.ai.ollamaI.rag.exception.DocumentEmbeddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class DocumentEmbeddingService {
    Logger logger = LoggerFactory.getLogger(DocumentEmbeddingService.class);

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final ResourcePatternResolver patternResolver;

    public DocumentEmbeddingService(VectorStore vectorStore, JdbcTemplate jdbcTemplate, ResourcePatternResolver patternResolver) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.patternResolver = patternResolver;
    }

    public void loadData(String path) throws DocumentEmbeddingException {
        logger.info("loadData");
        try {
            Resource[] resources = patternResolver.getResources(path);
            for (Resource resource : resources) {
                processFile(resource);
            }
        } catch (IOException | DocumentEmbeddingException e) {
            logger.error("IOException Occurred while loading the file : ");
            throw new DocumentEmbeddingException(DocumentEmbeddingErrorResponse.builder().errorMessage(e.getMessage()).build());
        }
    }

    private void processFile(Resource resource) throws DocumentEmbeddingException {
        logger.info("processFile");

        String currentMD5Hash = calculateMD5Hash(resource);

        String fileName = resource.getFilename();
        String existingMD5Hash = fetchExistingMD5Hash(fileName);
        if (currentMD5Hash.equals(existingMD5Hash)) {
            logger.info("Skipping {}; file unchanged.", fileName);
            return;
        }
        logger.info("File Changes are detected in {}. Re-indexing the ", fileName);

        vectorStore.delete("file_name == '" + fileName + "'");
        //Read the pdf
        TikaDocumentReader documentReader = new TikaDocumentReader(resource);
        //Split the content into chunks
        TextSplitter textSplitter = new TokenTextSplitter(400, 200, 20, 50, true);
        List<Document> documents = textSplitter.split(documentReader.read());
        //Store the data into vector database

        documents.forEach(document -> document.getMetadata().put("file_name", fileName));
        vectorStore.accept(documents);
        updateTrackingTable(fileName, currentMD5Hash);

    }

    private void updateTrackingTable(String fileName, String currentMD5Hash) {
        logger.info("updateTrackingTable");
        jdbcTemplate.update("INSERT INTO processed_files (file_name, md5_hash, last_processed) " +
                        "VALUES (?, ?, CURRENT_TIMESTAMP) " +
                        "ON CONFLICT (file_name) DO UPDATE SET md5_hash = EXCLUDED.md5_hash, last_processed = CURRENT_TIMESTAMP",
                fileName, currentMD5Hash);

    }

    private String calculateMD5Hash(Resource resource) throws DocumentEmbeddingException {
        logger.info("calculateMD5Hash");
        try (InputStream is = resource.getInputStream()) {
            return DigestUtils.md5DigestAsHex(is);
        } catch (IOException e) {
            logger.error("IOException Occurred while calculating the md5 hash : ");
            throw new DocumentEmbeddingException(DocumentEmbeddingErrorResponse.builder().errorMessage(e.getMessage()).build());
        }
    }

    private String fetchExistingMD5Hash(String fileName) {
        logger.info("existingMD5Hash");
        try {
            return jdbcTemplate.queryForObject("SELECT md5_hash FROM processed_files WHERE file_name = ?", String.class, fileName);
        } catch (Exception exception) {
            logger.warn("IOException Occurred while fetching the current md5 hash  from database and returning empty : ");
            return "";
        }
    }
}
