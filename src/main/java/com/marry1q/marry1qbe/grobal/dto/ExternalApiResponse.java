package com.marry1q.marry1qbe.grobal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
}
