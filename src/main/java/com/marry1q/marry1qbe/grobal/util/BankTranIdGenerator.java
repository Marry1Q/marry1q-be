package com.marry1q.marry1qbe.grobal.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class BankTranIdGenerator {
    
    private static final String PREFIX = "BANKTRAN";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();
    
    /**
     * bankTranId 생성
     * 형식: BANKTRAN + 현재시간(14자리) + 랜덤숫자(6자리)
     * 예: BANKTRAN20241201143000123456
     */
    public static String generateBankTranId() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String random = String.format("%06d", RANDOM.nextInt(1000000));
        return PREFIX + timestamp + random;
    }
    
    /**
     * 특정 시간으로 bankTranId 생성 (테스트용)
     */
    public static String generateBankTranId(LocalDateTime dateTime) {
        String timestamp = dateTime.format(TIMESTAMP_FORMATTER);
        String random = String.format("%06d", RANDOM.nextInt(1000000));
        return PREFIX + timestamp + random;
    }
}
