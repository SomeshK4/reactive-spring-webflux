package com.reactivespring.exceptionhandler;


import com.reactivespring.exception.MoviesInfoClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(MoviesInfoClientException.class)
    public ResponseEntity<String> handleClientException(MoviesInfoClientException moviesInfoClientException) {
        log.error("Exception caught in  handleClientException : {}", moviesInfoClientException);
        return ResponseEntity.status(moviesInfoClientException.getStatusCode())
                .body(moviesInfoClientException.getMessage());
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException runtimeException) {
        log.error("Exception caught in  handleRuntimeException", runtimeException);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(runtimeException.getMessage());
    }
}
