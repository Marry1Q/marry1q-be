package com.marry1q.marry1qbe.domain.giftMoney.dto.request;

import com.marry1q.marry1qbe.domain.giftMoney.enums.Relationship;
import com.marry1q.marry1qbe.domain.giftMoney.enums.Source;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "축의금 생성 요청")
public class CreateGiftMoneyRequest {
    
    @NotBlank(message = "축의자 이름은 필수입니다.")
    @Size(max = 100, message = "축의자 이름은 100자를 초과할 수 없습니다.")
    @Schema(description = "축의자 이름", example = "김철수", maxLength = 100)
    private String name;
    
    @NotNull(message = "축의금 금액은 필수입니다.")
    @DecimalMin(value = "1000", message = "축의금은 최소 1,000원 이상이어야 합니다.")
    @DecimalMax(value = "10000000", message = "축의금은 최대 10,000,000원을 초과할 수 없습니다.")
    @Schema(description = "축의금 금액", example = "50000", minimum = "1000", maximum = "10000000")
    private BigDecimal amount;
    
    @NotNull(message = "관계는 필수입니다.")
    @Schema(description = "관계", example = "FRIEND", allowableValues = {"FAMILY", "RELATIVE", "FRIEND", "COLLEAGUE", "ACQUAINTANCE", "OTHER"})
    private Relationship relationship;
    
    @NotNull(message = "받은방법은 필수입니다.")
    @Schema(description = "받은방법", example = "CASH", allowableValues = {"CASH", "TRANSFER"})
    private Source source;
    
    @Pattern(regexp = "^[0-9-]+$", message = "연락처 형식이 올바르지 않습니다.")
    @Size(max = 20, message = "연락처는 20자를 초과할 수 없습니다.")
    @Schema(description = "연락처", example = "010-1234-5678", maxLength = 20)
    private String phone;
    
    @Size(max = 1000, message = "주소는 1000자를 초과할 수 없습니다.")
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123", maxLength = 1000)
    private String address;
    
    @Size(max = 1000, message = "메모는 1000자를 초과할 수 없습니다.")
    @Schema(description = "메모", example = "정말 감사합니다!", maxLength = 1000)
    private String memo;
    
    @NotNull(message = "축의 날짜는 필수입니다.")
    @PastOrPresent(message = "축의 날짜는 과거 또는 오늘 날짜여야 합니다.")
    @Schema(description = "축의 날짜", example = "2024-01-15")
    private LocalDate giftDate;
}
