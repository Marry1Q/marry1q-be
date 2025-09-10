package com.marry1q.marry1qbe.grobal.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {
    private String status;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "Asia/Seoul")
    private LocalDateTime timestamp;
    
    public static HealthCheckResponse up() {
        return new HealthCheckResponse(
            "UP", 
            "Server is running", 
            LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        );
    }
    
    public static HealthCheckResponse down() {
        return new HealthCheckResponse(
            "DOWN", 
            "Server is down", 
            LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        );
    }
}
