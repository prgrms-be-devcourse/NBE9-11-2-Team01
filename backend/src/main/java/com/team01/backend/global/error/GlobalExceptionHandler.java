package com.team01.backend.global.error;


import com.team01.backend.global.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 기본 예외 처리. 응답 본문은 항상 {@link ApiResponse} 형태로 맞춘다. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String detail =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                err ->
                                        err.getField()
                                                + ": "
                                                + Objects.requireNonNullElse(
                                                err.getDefaultMessage(), "(사유 없음)"))
                        .collect(Collectors.joining(" / "));
        String message =
                detail.isEmpty()
                        ? "입력값이 올바르지 않습니다."
                        : "입력값이 올바르지 않습니다. (" + detail + ")";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.ofFailure("INVALID_INPUT", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.ofFailure(
                                "INVALID_INPUT",
                                Objects.requireNonNullElse(
                                        ex.getMessage(), "요청을 처리할 수 없습니다.")));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.ofFailure("NOT_FOUND", "요청하신 데이터를 찾을 수 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse.ofFailure(
                                "INTERNAL_ERROR",
                                "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.ofFailure("INVALID_JSON", "잘못된 형식의 JSON 데이터입니다."));
    }
}