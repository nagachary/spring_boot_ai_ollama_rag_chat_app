package com.naga.ai.ollamaI.rag.exception;

public record DocumentEmbeddingErrorResponse(String errorMessage, String errorCode) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String errorMessage;
        private String errorCode;

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public DocumentEmbeddingErrorResponse build() {
            return new DocumentEmbeddingErrorResponse(errorMessage, errorCode);
        }
    }
}
