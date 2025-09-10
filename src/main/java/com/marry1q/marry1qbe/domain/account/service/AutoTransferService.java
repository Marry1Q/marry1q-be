package com.marry1q.marry1qbe.domain.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marry1q.marry1qbe.domain.account.dto.request.AutoTransferCreateRequest;
import com.marry1q.marry1qbe.domain.account.dto.request.AutoTransferUpdateRequest;
import com.marry1q.marry1qbe.domain.account.dto.response.AutoTransferResponse;
import com.marry1q.marry1qbe.domain.account.dto.response.ProductPaymentInfoResponse;
import com.marry1q.marry1qbe.domain.account.service.external.HanaBankAutoTransferApiClient;
import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.customer.repository.CustomerRepository;
import com.marry1q.marry1qbe.grobal.exception.CustomException;
import com.marry1q.marry1qbe.grobal.commonCode.ErrorCode;
import com.marry1q.marry1qbe.grobal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AutoTransferService {
    
    private final HanaBankAutoTransferApiClient hanaBankAutoTransferApiClient;
    private final SecurityUtil securityUtil;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 자동이체 등록
     */
    public AutoTransferResponse createAutoTransfer(AutoTransferCreateRequest request) {
        log.info("자동이체 등록 시작 - 계좌주명: {}, 금액: {}, 주기: {}", 
                 request.getToAccountName(), request.getAmount(), request.getFrequency());
        
        try {
            // 1. 현재 사용자의 userSeqNo 조회
            String userSeqNo = securityUtil.getCurrentUserSeqNo();
            
            // 2. userSeqNo로 userCi 조회
            String userCi = getUserCiFromUserSeqNo(userSeqNo);
            
            // 3. 하나은행 API 요청 데이터 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userCi", userCi);
            requestBody.put("fromAccountNumber", request.getFromAccountNumber());
            requestBody.put("toAccountNumber", request.getToAccountNumber());
            requestBody.put("toAccountName", request.getToAccountName());
            requestBody.put("toBankCode", request.getToBankCode());
            requestBody.put("amount", request.getAmount());
            requestBody.put("schedule", request.getFrequency());
            requestBody.put("memo", request.getMemo() != null ? request.getMemo() : "Plan1Q 자동이체");
            requestBody.put("periodMonths", request.getPeriodMonths());
            requestBody.put("status", request.getInitialStatus() != null ? request.getInitialStatus() : "ACTIVE");
            requestBody.put("lastExecutionStatus", request.getInitialStatus() != null ? request.getInitialStatus() : "PENDING");
            requestBody.put("currentInstallment", request.getCurrentInstallment() != null ? request.getCurrentInstallment() : 1);
            requestBody.put("remainingInstallments", request.getRemainingInstallments() != null ? request.getRemainingInstallments() : 
                           (request.getPeriodMonths() != null ? request.getPeriodMonths() - 1 : 11));
            requestBody.put("lastExecutionDate", request.getLastExecutionDate() != null ? request.getLastExecutionDate().toString() : null);
            
            // 4. 하나은행 API 호출
            Map<String, Object> response = hanaBankAutoTransferApiClient.createAutoTransfer(requestBody);
            
            // 5. 응답 데이터를 AutoTransferResponse로 변환
            // HanaBankAutoTransferApiClient.createAutoTransfer()는 callApi를 호출하고,
            // callApi는 이미 responseBody.get("data")를 반환하므로
            // response가 바로 data 필드의 내용입니다.
            if (response == null) {
                log.error("자동이체 등록 응답이 null입니다.");
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 등록 응답에 데이터가 없습니다.");
            }
            
            if (!(response instanceof Map)) {
                log.error("자동이체 등록 응답이 Map이 아닙니다. 응답 타입: {}, 응답: {}", response.getClass(), response);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 등록 응답 형식이 올바르지 않습니다.");
            }
            
            AutoTransferResponse autoTransferResponse = convertMapToResponse((Map<String, Object>) response);
            
            log.info("자동이체 등록 완료 - ID: {}, 다음 이체일: {}", 
                     autoTransferResponse.getAutoTransferId(), autoTransferResponse.getNextTransferDate());
            
            return autoTransferResponse;
            
        } catch (CustomException e) {
            log.error("자동이체 등록 실패 - CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("자동이체 등록 중 예외 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 등록에 실패했습니다.");
        }
    }
    
    /**
     * 자동이체 등록 (상태 정보 포함)
     */
    public AutoTransferResponse createAutoTransferWithStatus(AutoTransferCreateRequest request, 
                                                            String status, String lastExecutionStatus) {
        log.info("자동이체 등록 시작 (상태 포함) - 계좌주명: {}, 금액: {}, 주기: {}, 상태: {}, 실행상태: {}", 
                 request.getToAccountName(), request.getAmount(), request.getFrequency(), status, lastExecutionStatus);
        
        try {
            // 1. 현재 사용자의 userSeqNo 조회
            String userSeqNo = securityUtil.getCurrentUserSeqNo();
            
            // 2. userSeqNo로 userCi 조회
            String userCi = getUserCiFromUserSeqNo(userSeqNo);
            
            // 3. 하나은행 API 요청 데이터 생성 (상태 정보 포함)
            Map<String, Object> requestBody = Map.of(
                "userCi", userCi,
                "fromAccountNumber", request.getFromAccountNumber(),
                "toAccountNumber", request.getToAccountNumber(),
                "toAccountName", request.getToAccountName(),
                "toBankCode", request.getToBankCode(),
                "amount", request.getAmount(),
                "schedule", request.getFrequency(),
                "memo", request.getMemo() != null ? request.getMemo() : "Plan1Q 자동이체",
                "status", status,                                    // 상태 정보 추가
                "lastExecutionStatus", lastExecutionStatus          // 실행 상태 추가
            );
            
            // 4. 하나은행 API 호출
            Map<String, Object> response = hanaBankAutoTransferApiClient.createAutoTransfer(requestBody);
            
            // 5. 응답 데이터를 AutoTransferResponse로 변환
            if (response == null) {
                log.error("자동이체 등록 응답이 null입니다.");
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 등록 응답에 데이터가 없습니다.");
            }
            
            if (!(response instanceof Map)) {
                log.error("자동이체 등록 응답이 Map이 아닙니다. 응답 타입: {}, 응답: {}", response.getClass(), response);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 등록 응답 형식이 올바르지 않습니다.");
            }
            
            AutoTransferResponse autoTransferResponse = convertMapToResponse((Map<String, Object>) response);
            
            log.info("자동이체 등록 완료 (상태 포함) - ID: {}, 상태: {}, 실행상태: {}, 다음 이체일: {}", 
                     autoTransferResponse.getAutoTransferId(), status, lastExecutionStatus, 
                     autoTransferResponse.getNextTransferDate());
            
            return autoTransferResponse;
            
        } catch (CustomException e) {
            log.error("자동이체 등록 실패 (상태 포함) - CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("자동이체 등록 중 예외 발생 (상태 포함)", e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 등록에 실패했습니다.");
        }
    }
    
    /**
     * 자동이체 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AutoTransferResponse> getAutoTransferList(String fromAccountNumber) {
        log.info("자동이체 목록 조회 시작 - 출금 계좌번호: {}", fromAccountNumber);
        
        try {
            // 1. 하나은행 API 호출
            Map<String, Object> response = hanaBankAutoTransferApiClient.listAutoTransfers(fromAccountNumber);
            
            // 2. 응답 데이터를 AutoTransferResponse 리스트로 변환
            // response는 {success: true, data: [...], message: "..."} 형태
            Object data = response.get("data");
            if (data == null) {
                log.warn("자동이체 목록 조회 응답에 data 필드가 없습니다. 응답: {}", response);
                return List.of();
            }
            
            List<Map<String, Object>> dataList;
            if (data instanceof List) {
                dataList = (List<Map<String, Object>>) data;
            } else {
                log.error("자동이체 목록 조회 응답의 data 필드가 List가 아닙니다. data 타입: {}, data: {}", data.getClass(), data);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 목록 조회 응답 형식이 올바르지 않습니다.");
            }
            
            List<AutoTransferResponse> autoTransferResponses = dataList.stream()
                    .map(this::convertMapToResponse)
                    .toList();
            
            log.info("자동이체 목록 조회 완료 - 조회 건수: {}", autoTransferResponses.size());
            
            return autoTransferResponses;
            
        } catch (CustomException e) {
            log.error("자동이체 목록 조회 실패 - CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("자동이체 목록 조회 중 예외 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 목록 조회에 실패했습니다.");
        }
    }
    
    /**
     * 자동이체 상세 조회
     */
    @Transactional(readOnly = true)
    public AutoTransferResponse getAutoTransferDetail(Long autoTransferId) {
        log.info("자동이체 상세 조회 시작 - ID: {}", autoTransferId);
        
        try {
            // 1. 하나은행 API 호출
            Map<String, Object> response = hanaBankAutoTransferApiClient.getAutoTransferDetail(autoTransferId);
            
            // 2. 응답 데이터를 AutoTransferResponse로 변환
            // response는 {success: true, data: {...}, message: "..."} 형태
            Object data = response.get("data");
            if (data == null) {
                log.error("자동이체 상세 조회 응답에 data 필드가 없습니다. 응답: {}", response);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 상세 조회 응답에 데이터가 없습니다.");
            }
            
            if (!(data instanceof Map)) {
                log.error("자동이체 상세 조회 응답의 data 필드가 Map이 아닙니다. data 타입: {}, data: {}", data.getClass(), data);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 상세 조회 응답 형식이 올바르지 않습니다.");
            }
            
            AutoTransferResponse autoTransferResponse = convertMapToResponse((Map<String, Object>) data);
            
            log.info("자동이체 상세 조회 완료 - ID: {}", autoTransferId);
            
            return autoTransferResponse;
            
        } catch (CustomException e) {
            log.error("자동이체 상세 조회 실패 - CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("자동이체 상세 조회 중 예외 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 상세 조회에 실패했습니다.");
        }
    }
    
    /**
     * 자동이체 수정
     */
    public AutoTransferResponse updateAutoTransfer(Long autoTransferId, AutoTransferUpdateRequest request) {
        log.info("자동이체 수정 시작 - ID: {}, 계좌주명: {}, 금액: {}, 주기: {}", 
                 autoTransferId, request.getToAccountName(), request.getAmount(), request.getFrequency());
        
        try {
            // 1. 하나은행 API 요청 데이터 생성
            Map<String, Object> requestBody = Map.of(
                "toAccountNumber", request.getToAccountNumber(),
                "toAccountName", request.getToAccountName(),
                "toBankCode", request.getToBankCode(),
                "amount", request.getAmount(),
                "schedule", request.getFrequency(),
                "memo", request.getMemo() != null ? request.getMemo() : "Plan1Q 자동이체"
            );
            
            // 2. 하나은행 API 호출
            Map<String, Object> response = hanaBankAutoTransferApiClient.updateAutoTransfer(autoTransferId, requestBody);
            
            // 3. 응답 데이터를 AutoTransferResponse로 변환
            // response는 {success: true, data: {...}, message: "..."} 형태
            Object data = response.get("data");
            if (data == null) {
                log.error("자동이체 수정 응답에 data 필드가 없습니다. 응답: {}", response);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 수정 응답에 데이터가 없습니다.");
            }
            
            if (!(data instanceof Map)) {
                log.error("자동이체 수정 응답의 data 필드가 Map이 아닙니다. data 타입: {}, data: {}", data.getClass(), data);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 수정 응답 형식이 올바르지 않습니다.");
            }
            
            AutoTransferResponse autoTransferResponse = convertMapToResponse((Map<String, Object>) data);
            
            log.info("자동이체 수정 완료 - ID: {}, 다음 이체일: {}", 
                     autoTransferResponse.getAutoTransferId(), autoTransferResponse.getNextTransferDate());
            
            return autoTransferResponse;
            
        } catch (CustomException e) {
            log.error("자동이체 수정 실패 - CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("자동이체 수정 중 예외 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 수정에 실패했습니다.");
        }
    }
    
    /**
     * 자동이체 삭제
     */
    public void deleteAutoTransfer(Long autoTransferId) {
        log.info("자동이체 삭제 시작 - ID: {}", autoTransferId);
        
        try {
            // 1. 하나은행 API 호출
            hanaBankAutoTransferApiClient.deleteAutoTransfer(autoTransferId);
            
            log.info("자동이체 삭제 완료 - ID: {}", autoTransferId);
            
        } catch (CustomException e) {
            log.error("자동이체 삭제 실패 - CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("자동이체 삭제 중 예외 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "자동이체 삭제에 실패했습니다.");
        }
    }
    
    /**
     * 상품별 자동이체 납입 정보 조회
     */
    @Transactional(readOnly = true)
    public List<ProductPaymentInfoResponse> getProductPaymentInfo(String toAccountNumber) {
        log.info("상품별 자동이체 납입 정보 조회 시작 - 입금 계좌번호: {}", toAccountNumber);
        
        try {
            // 1. 하나은행 API 호출
            Map<String, Object> response = hanaBankAutoTransferApiClient.getIncomingAutoTransfers(toAccountNumber);
            
            // 2. 응답 데이터를 ProductPaymentInfoResponse 리스트로 변환
            // response는 {success: true, data: [...], message: "..."} 형태
            Object data = response.get("data");
            if (data == null) {
                log.warn("상품별 자동이체 납입 정보 조회 응답에 data 필드가 없습니다. 응답: {}", response);
                return List.of();
            }
            
            List<Map<String, Object>> dataList;
            if (data instanceof List) {
                dataList = (List<Map<String, Object>>) data;
            } else {
                log.error("상품별 자동이체 납입 정보 조회 응답의 data 필드가 List가 아닙니다. data 타입: {}, data: {}", data.getClass(), data);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "상품별 자동이체 납입 정보 조회 응답 형식이 올바르지 않습니다.");
            }
            
            List<ProductPaymentInfoResponse> productPaymentInfoResponses = dataList.stream()
                    .map(this::convertMapToProductPaymentInfoResponse)
                    .toList();
            
            log.info("상품별 자동이체 납입 정보 조회 완료 - 조회 건수: {}", productPaymentInfoResponses.size());
            
            return productPaymentInfoResponses;
            
        } catch (CustomException e) {
            log.error("상품별 자동이체 납입 정보 조회 실패 - CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("상품별 자동이체 납입 정보 조회 중 예외 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "상품별 자동이체 납입 정보 조회에 실패했습니다.");
        }
    }
    
    /**
     * userSeqNo를 userCi로 변환
     */
    private String getUserCiFromUserSeqNo(String userSeqNo) {
        log.info("userSeqNo를 userCi로 변환 시작 - userSeqNo: {}", userSeqNo);
        
        Customer customer = customerRepository.findById(userSeqNo)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다. userSeqNo: {}", userSeqNo);
                    return new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
                });
        
        String userCi = customer.getUserCi();
        log.info("userCi 변환 완료 - userSeqNo: {} -> userCi: {}", userSeqNo, userCi);
        
        return userCi;
    }
    
    /**
     * Map을 AutoTransferResponse로 변환
     */
    private AutoTransferResponse convertMapToResponse(Map<String, Object> data) {
        if (data == null) {
            log.error("convertMapToResponse: data가 null입니다.");
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "응답 데이터가 null입니다.");
        }
        
        return AutoTransferResponse.builder()
                .autoTransferId(Long.valueOf(data.get("autoTransferId").toString()))
                .toAccountNumber((String) data.get("toAccountNumber"))
                .toAccountName((String) data.get("toAccountName"))
                .toBankCode((String) data.get("toBankCode"))
                .amount(new BigDecimal(data.get("amount").toString()))
                .schedule((String) data.get("schedule"))
                .nextTransferDate(parseDateSafely(data.get("nextTransferDate")))
                .memo((String) data.get("memo"))
                .status((String) data.get("status"))
                .createdAt(parseDateSafely(data.get("createdAt")))
                .updatedAt(parseDateSafely(data.get("updatedAt")))
                .lastExecutionStatus((String) data.get("lastExecutionStatus"))
                .userCi((String) data.get("userCi"))
                .totalInstallments((Integer) data.get("totalInstallments"))
                .currentInstallment((Integer) data.get("currentInstallment"))
                .remainingInstallments((Integer) data.get("remainingInstallments"))
                .lastExecutionDate(parseDateSafely(data.get("lastExecutionDate")))
                .build();
    }

    /**
     * Map을 ProductPaymentInfoResponse로 변환
     */
    private ProductPaymentInfoResponse convertMapToProductPaymentInfoResponse(Map<String, Object> data) {
        if (data == null) {
            log.error("convertMapToProductPaymentInfoResponse: data가 null입니다.");
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "응답 데이터가 null입니다.");
        }
        
        return ProductPaymentInfoResponse.builder()
                .autoTransferId(Long.valueOf(data.get("autoTransferId").toString()))
                .fromAccountNumber((String) data.get("fromAccountNumber"))
                .toAccountNumber((String) data.get("toAccountNumber"))
                .amount(new BigDecimal(data.get("amount").toString()))
                .nextPaymentDate(parseDateSafely(data.get("nextPaymentDate")))
                .currentInstallment((Integer) data.get("currentInstallment"))
                .totalInstallments((Integer) data.get("totalInstallments"))
                .remainingInstallments((Integer) data.get("remainingInstallments"))
                .paymentStatus((String) data.get("paymentStatus"))
                .isFirstInstallment((Boolean) data.get("isFirstInstallment"))
                .lastExecutionDate(parseDateSafely(data.get("lastExecutionDate")))
                .build();
    }
    
    /**
     * 안전한 날짜 파싱 메서드
     * LocalDateTime 형식이나 LocalDate 형식 모두 처리 가능
     */
    private LocalDate parseDateSafely(Object dateValue) {
        if (dateValue == null) {
            return null;
        }
        
        String dateStr = dateValue.toString();
        
        try {
            // 먼저 LocalDate 형식으로 시도
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            try {
                // LocalDateTime 형식인 경우 날짜 부분만 추출
                if (dateStr.contains("T")) {
                    String datePart = dateStr.substring(0, dateStr.indexOf("T"));
                    return LocalDate.parse(datePart);
                } else {
                    // 다른 형식인 경우 로그 출력 후 null 반환
                    log.warn("알 수 없는 날짜 형식: {}", dateStr);
                    return null;
                }
            } catch (Exception ex) {
                log.warn("날짜 파싱 실패: {}", dateStr, ex);
                return null;
            }
        }
    }
    
}
