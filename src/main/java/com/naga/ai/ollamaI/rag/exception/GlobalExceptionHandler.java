package com.naga.ai.ollamaI.rag.exception;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.ConnectException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    @ExceptionHandler(value = HttpClientErrorException.class)
    public DocumentEmbeddingErrorResponse tooManyRequestsException(HttpClientErrorException exception) {
        return new DocumentEmbeddingErrorResponse(exception.getMessage(), HttpStatus.TOO_MANY_REQUESTS.toString());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            NoResourceFoundException.class, TransactionException.class})
    public DocumentEmbeddingErrorResponse handleNoResourceException(NoResourceFoundException exception) {
        return new DocumentEmbeddingErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND.toString());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({DocumentEmbeddingException.class, ConnectException.class, ResourceAccessException.class})
    public DocumentEmbeddingErrorResponse handleGitHubServiceException(Exception exception) {
        return new DocumentEmbeddingErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.toString());
    }
}
