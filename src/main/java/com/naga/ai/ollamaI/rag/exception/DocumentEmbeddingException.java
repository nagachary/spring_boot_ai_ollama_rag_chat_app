package com.naga.ai.ollamaI.rag.exception;

public class DocumentEmbeddingException extends Exception {

    private final DocumentEmbeddingErrorResponse errorResponse;

    public DocumentEmbeddingException(DocumentEmbeddingErrorResponse errorResponse) {
        super(errorResponse.errorMessage());
        this.errorResponse = errorResponse;
    }

    public DocumentEmbeddingErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
