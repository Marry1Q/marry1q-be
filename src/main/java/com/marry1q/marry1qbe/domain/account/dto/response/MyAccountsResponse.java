package com.marry1q.marry1qbe.domain.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "내 계좌 정보 조회 응답")
public class MyAccountsResponse {
    
    @Schema(description = "계좌 목록")
    private List<AccountInfo> accounts;
    
    @Schema(description = "총 계좌 수", example = "3")
    private Integer totalCount;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "계좌 정보")
    public static class AccountInfo {
        
        @Schema(description = "계좌 ID", example = "1")
        private Long accountId;
        
        @Schema(description = "은행 코드", example = "081")
        private String bank;
        
        @Schema(description = "계좌번호", example = "110-123456-789012")
        private String accountNumber;
        
        @Schema(description = "계좌명", example = "김철수 입출금통장")
        private String accountName;
        
        @Schema(description = "현재 잔액", example = "1000000")
        private BigDecimal balance;
        
        @Schema(description = "모임통장 여부", example = "false")
        private Boolean isCoupleAccount;
        
        @Schema(description = "사용자 고유 번호", example = "U001")
        private String userSeqNo;
        
        @Schema(description = "마지막 동기화 시간")
        private LocalDateTime lastSyncedAt;
        
        @Schema(description = "잔액 조회 상태", example = "SUCCESS")
        private String balanceStatus;
        
        @Schema(description = "에러 메시지 (잔액 조회 실패 시)")
        private String errorMessage;
    }
}
