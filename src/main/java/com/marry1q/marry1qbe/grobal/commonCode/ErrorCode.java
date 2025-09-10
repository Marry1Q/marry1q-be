package com.marry1q.marry1qbe.grobal.commonCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 인증 관련 에러
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD("INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),
    INVALID_PIN("INVALID_PIN", "핀 번호가 올바르지 않습니다."),
    PIN_LOCKED("PIN_LOCKED", "핀 번호가 잠겼습니다. 잠시 후 다시 시도해주세요."),
    USER_NOT_FOUND("USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "토큰이 만료되었습니다."),
    
    // 일반 에러
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    INVALID_REQUEST("INVALID_REQUEST", "잘못된 요청입니다."),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다."),
    
    // 축의금 관련 에러
    GIFT_MONEY_NOT_FOUND("GIFT_MONEY_NOT_FOUND", "축의금을 찾을 수 없습니다."),
    GIFT_MONEY_STATS_NOT_FOUND("GIFT_MONEY_STATS_NOT_FOUND", "축의금 통계를 찾을 수 없습니다."),
    GIFT_MONEY_CREATE_FAILED("GIFT_MONEY_CREATE_FAILED", "축의금 생성에 실패했습니다."),
    GIFT_MONEY_UPDATE_FAILED("GIFT_MONEY_UPDATE_FAILED", "축의금 수정에 실패했습니다."),
    GIFT_MONEY_DELETE_FAILED("GIFT_MONEY_DELETE_FAILED", "축의금 삭제에 실패했습니다."),
    GIFT_MONEY_STATS_UPDATE_FAILED("GIFT_MONEY_STATS_UPDATE_FAILED", "축의금 통계 업데이트에 실패했습니다."),
    
    // 청첩장 관련 에러
    INVITATION_NOT_FOUND("INVITATION_NOT_FOUND", "청첩장이 존재하지 않습니다."),
    INVITATION_ACCESS_DENIED("INVITATION_ACCESS_DENIED", "결혼식이 종료되었습니다."),
    INVITATION_CREATE_FAILED("INVITATION_CREATE_FAILED", "청첩장 생성에 실패했습니다."),
    INVITATION_UPDATE_FAILED("INVITATION_UPDATE_FAILED", "청첩장 수정에 실패했습니다."),
    INVITATION_DELETE_FAILED("INVITATION_DELETE_FAILED", "청첩장 삭제에 실패했습니다."),
    
    // Plan1Q 관련 에러
    PLAN1Q_GOAL_NOT_FOUND("PLAN1Q_GOAL_NOT_FOUND", "Plan1Q 목표를 찾을 수 없습니다."),
    INVESTMENT_PROFILE_NOT_FOUND("INVESTMENT_PROFILE_NOT_FOUND", "투자성향 검사 결과를 찾을 수 없습니다."),
    INVESTMENT_PROFILE_EXPIRED("INVESTMENT_PROFILE_EXPIRED", "투자성향 검사가 만료되었습니다."),
    INSUFFICIENT_INVESTMENT_PROFILE("INSUFFICIENT_INVESTMENT_PROFILE", "투자성향 검사가 필요합니다."),
    PLAN1Q_PRODUCT_NOT_FOUND("PLAN1Q_PRODUCT_NOT_FOUND", "Plan1Q 상품을 찾을 수 없습니다."),
    
    // 외부 API 연동 관련 에러
    EXTERNAL_API_ERROR("EXTERNAL_API_ERROR", "외부 API 연동 중 오류가 발생했습니다."),
    
    // 계좌 관련 에러
    ACCOUNT_REGISTER_FAILED("ACCOUNT_REGISTER_FAILED", "계좌 등록에 실패했습니다."),
    INTEGRATED_ACCOUNT_LIST_FAILED("INTEGRATED_ACCOUNT_LIST_FAILED", "계좌통합조회에 실패했습니다."),
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "계좌를 찾을 수 없습니다."),
    ACCOUNT_ALREADY_EXISTS("ACCOUNT_ALREADY_EXISTS", "이미 등록된 계좌입니다."),
    CUSTOMER_NOT_FOUND("CUSTOMER_NOT_FOUND", "사용자 정보를 찾을 수 없습니다."),
    
    // 자동이체 관련 에러
    AUTO_TRANSFER_REGISTRATION_FAILED("AUTO_TRANSFER_REGISTRATION_FAILED", "자동이체 등록에 실패했습니다."),
    
    // S3 관련 에러
    S3_UPLOAD_FAILED("S3_UPLOAD_FAILED", "파일 업로드에 실패했습니다."),
    S3_DELETE_FAILED("S3_DELETE_FAILED", "파일 삭제에 실패했습니다."),
    S3_URL_INVALID("S3_URL_INVALID", "유효하지 않은 S3 URL입니다."),
    
    // DB 관련 에러
    DB_SAVE_FAILED("DB_SAVE_FAILED", "데이터 저장에 실패했습니다."),
    DB_UPDATE_FAILED("DB_UPDATE_FAILED", "데이터 수정에 실패했습니다."),
    DB_DELETE_FAILED("DB_DELETE_FAILED", "데이터 삭제에 실패했습니다."),
    
    COUPLE_NOT_FOUND("COUPLE_NOT_FOUND", "커플 정보를 찾을 수 없습니다."),
    NO_COUPLE("NO_COUPLE", "속해있는 커플이 없습니다."),
    REPRESENTATIVE_INVITATION_NOT_FOUND("REPRESENTATIVE_INVITATION_NOT_FOUND", "대표 청첩장이 없습니다.");

    private final String code;
    private final String message;
}
