package com.marry1q.marry1qbe.grobal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private ErrorResponse error;
    private LocalDateTime timestamp;

    public static <T> CustomApiResponse<T> success(T data) {
        CustomApiResponse<T> response = new CustomApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage("성공적으로 처리되었습니다.");
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> CustomApiResponse<T> success(T data, String message) {
        CustomApiResponse<T> response = new CustomApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> CustomApiResponse<T> error(String code, String message) {
        CustomApiResponse<T> response = new CustomApiResponse<>();
        response.setSuccess(false);
        response.setError(new ErrorResponse(code, message));
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
