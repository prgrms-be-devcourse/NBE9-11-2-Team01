package com.team01.backend.global.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * API 공통 응답 형태. 성공·실패 모두 동일한 JSON 필드로 맞춘다.
 *
 * <ul>
 *   <li>성공: {@link #ofSuccess(Object)}, {@link #ofSuccessWithoutBody()}
 *   <li>실패: {@link com.team01.backend.global.error.GlobalExceptionHandler}가 {@link #ofFailure(String, String)}로
 *       채움 (컨트롤러에서 직접 써도 됨)
 * </ul>
 */
@JsonInclude(Include.NON_NULL)
public record ApiResponse<T>(boolean success, String code, String message, T data) {

    public static <T> ApiResponse<T> ofSuccess(T data) {
        return new ApiResponse<>(true, null, null, data);
    }

    public static ApiResponse<Void> ofSuccessWithoutBody() {
        return new ApiResponse<>(true, null, null, null);
    }

    public static <T> ApiResponse<T> ofFailure(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    public static <T> ApiResponse<T> ofFailure(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }

}