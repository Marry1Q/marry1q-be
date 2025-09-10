package com.marry1q.marry1qbe.domain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "고객 정보 응답")
public class CustomerInfoResponse {
    
    @Schema(description = "사용자 고유 번호", example = "U001")
    private String userSeqNo;
    
    @Schema(description = "고객명", example = "홍길동")
    private String customerName;
    
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String customerPhone;
    
    @Schema(description = "이메일", example = "kim@example.com")
    private String customerEmail;
    
    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;
    
    @Schema(description = "커플 URL 슬러그", example = "couple-123")
    private String coupleSlug;
    
    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일시", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}
