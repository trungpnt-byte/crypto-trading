package com.aquarius.crypto.exception;


import com.aquarius.crypto.common.LocalApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<LocalApiResponse<Void>>> handleAuthenticationException(
            AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(LocalApiResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED.value())));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public Mono<ResponseEntity<LocalApiResponse<Void>>> handleInsufficientBalanceException(
            InsufficientBalanceException ex) {
        log.error("Insufficient balance: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(LocalApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value())));
    }

//    @ExceptionHandler(PriceNotFoundException.class)
//    public Mono<ResponseEntity<ApiResponse<Void>>> handlePriceNotFoundException(
//            PriceNotFoundException ex) {
//        log.error("Price not found: {}", ex.getMessage());
//        return Mono.just(ResponseEntity
//                .status(HttpStatus.NOT_FOUND)
//                .body(ApiResponse.error(ex.getMessage())));
//    }
//
//    @ExceptionHandler(TradingException.class)
//    public Mono<ResponseEntity<ApiResponse<Void>>> handleTradingException(
//            TradingException ex) {
//        log.error("Trading error: {}", ex.getMessage(), ex);
//        return Mono.just(ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Trading operation failed: " + ex.getMessage())));
//    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<LocalApiResponse<Map<String, String>>>> handleValidationException(
            WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation error: {}", errors);

        LocalApiResponse<Map<String, String>> response = LocalApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<LocalApiResponse<Void>>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(LocalApiResponse.error("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value())));
    }
}
