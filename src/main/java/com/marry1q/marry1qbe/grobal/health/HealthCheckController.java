package com.marry1q.marry1qbe.grobal.health;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "헬스 체크", description = "배포 서버 상태 확인용 API")
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<HealthCheckResponse> healthCheck() {
        log.info("Health check request received");
        
        try {
            HealthCheckResponse response = HealthCheckResponse.up();
            log.info("Health check completed successfully - Status: {}", response.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Health check failed", e);
            HealthCheckResponse response = HealthCheckResponse.down();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
