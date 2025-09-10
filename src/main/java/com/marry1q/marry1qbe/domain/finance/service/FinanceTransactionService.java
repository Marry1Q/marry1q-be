package com.marry1q.marry1qbe.domain.finance.service;

import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import com.marry1q.marry1qbe.domain.customer.entity.Customer;
import com.marry1q.marry1qbe.domain.customer.repository.CustomerRepository;
import com.marry1q.marry1qbe.domain.finance.dto.request.CreateTransactionRequest;
import com.marry1q.marry1qbe.domain.finance.dto.request.TransactionSearchRequest;
import com.marry1q.marry1qbe.domain.finance.dto.request.UpdateTransactionRequest;
import com.marry1q.marry1qbe.domain.finance.dto.response.TransactionResponse;
import com.marry1q.marry1qbe.domain.finance.entity.FinanceCategory;
import com.marry1q.marry1qbe.domain.finance.entity.FinanceTransaction;
import com.marry1q.marry1qbe.domain.finance.exception.FinanceTransactionNotFoundException;
import com.marry1q.marry1qbe.domain.finance.repository.FinanceCategoryRepository;
import com.marry1q.marry1qbe.domain.finance.repository.FinanceTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.marry1q.marry1qbe.domain.finance.exception.FinanceCategoryNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceTransactionService {
    
    private final FinanceTransactionRepository financeTransactionRepository;
    private final FinanceCategoryRepository financeCategoryRepository;
    private final CustomerRepository customerRepository;
    private final CategoryBudgetService categoryBudgetService;
    private final CoupleService coupleService;
    
    /**
     * 거래 내역 생성
     */
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, String userSeqNo, Long coupleId) {
        // 카테고리 존재 여부 확인
        FinanceCategory category = financeCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new FinanceCategoryNotFoundException("카테고리를 찾을 수 없습니다: " + request.getCategoryId()));
        
        // 카테고리가 해당 커플의 것인지 확인
        if (!category.getCoupleId().equals(coupleId)) {
            throw new FinanceCategoryNotFoundException("해당 커플의 카테고리가 아닙니다: " + request.getCategoryId());
        }
        
        // 거래 금액을 음수로 변환 (지출인 경우)
        BigDecimal amount = request.getTransactionType() == FinanceTransaction.TransactionType.EXPENSE 
                ? request.getAmount().negate() 
                : request.getAmount();
        
        // 거래 내역 생성
        FinanceTransaction transaction = FinanceTransaction.builder()
                .amount(amount)
                .transactionType(request.getTransactionType())
                .description(request.getDescription())
                .memo(request.getMemo())
                .transactionDate(request.getTransactionDate())
                .transactionTime(request.getTransactionTime())
                .financeCategory(category)
                .userSeqNo(userSeqNo)
                .coupleId(coupleId)
                .build();
        
        FinanceTransaction savedTransaction = financeTransactionRepository.save(transaction);
        
        // 지출인 경우에만 카테고리 예산의 spent_amount 업데이트
        if (request.getTransactionType() == FinanceTransaction.TransactionType.EXPENSE) {
            try {
                categoryBudgetService.increaseSpentAmount(category.getFinanceCategoryId(), request.getAmount());
            } catch (Exception e) {
                log.warn("카테고리 예산 업데이트 중 오류가 발생했습니다. 카테고리 ID: {}, 오류: {}", 
                        category.getFinanceCategoryId(), e.getMessage());
            }
            coupleService.increaseCurrentSpent(coupleId, request.getAmount());
        }
        
        return TransactionResponse.from(savedTransaction);
    }
    
    /**
     * 거래 내역 수정
     */
    @Transactional
    public TransactionResponse updateTransaction(Long transactionId, UpdateTransactionRequest request, Long coupleId) {
        // 기존 거래 내역 조회
        FinanceTransaction transaction = financeTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new FinanceTransactionNotFoundException("거래 내역을 찾을 수 없습니다: " + transactionId));
        
        // 권한 확인
        if (!transaction.getCoupleId().equals(coupleId)) {
            throw new FinanceTransactionNotFoundException("해당 커플의 거래 내역이 아닙니다: " + transactionId);
        }
        
        // 기존 거래가 지출이었다면 커플의 current_spent와 카테고리 예산에서 차감
        if (transaction.getTransactionType() == FinanceTransaction.TransactionType.EXPENSE) {
            coupleService.decreaseCurrentSpent(coupleId, transaction.getAmount().abs());
            try {
                categoryBudgetService.decreaseSpentAmount(transaction.getFinanceCategory().getFinanceCategoryId(), transaction.getAmount().abs());
            } catch (Exception e) {
                log.warn("기존 거래의 카테고리 예산 차감 중 오류가 발생했습니다. 카테고리 ID: {}, 오류: {}", 
                        transaction.getFinanceCategory().getFinanceCategoryId(), e.getMessage());
            }
        }
        
        // 새 카테고리 존재 여부 확인
        FinanceCategory newCategory = financeCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new FinanceCategoryNotFoundException("카테고리를 찾을 수 없습니다: " + request.getCategoryId()));
        
        // 새 카테고리가 해당 커플의 것인지 확인
        if (!newCategory.getCoupleId().equals(coupleId)) {
            throw new FinanceCategoryNotFoundException("해당 커플의 카테고리가 아닙니다: " + request.getCategoryId());
        }
        
        // 거래 금액을 음수로 변환 (지출인 경우)
        BigDecimal amount = request.getTransactionType() == FinanceTransaction.TransactionType.EXPENSE 
                ? request.getAmount().negate() 
                : request.getAmount();
        
        // 거래 내역 업데이트
        transaction.update(request.getDescription(), request.getMemo(), request.getTransactionDate(), 
                          request.getTransactionTime(), amount, request.getTransactionType(), newCategory);
        
        FinanceTransaction updatedTransaction = financeTransactionRepository.save(transaction);
        
        // 새 거래가 지출인 경우에만 카테고리 예산의 spent_amount와 커플의 current_spent 업데이트
        if (request.getTransactionType() == FinanceTransaction.TransactionType.EXPENSE) {
            try {
                categoryBudgetService.increaseSpentAmount(newCategory.getFinanceCategoryId(), request.getAmount());
            } catch (Exception e) {
                log.warn("새 거래의 카테고리 예산 증가 중 오류가 발생했습니다. 카테고리 ID: {}, 오류: {}", 
                        newCategory.getFinanceCategoryId(), e.getMessage());
            }
            coupleService.increaseCurrentSpent(coupleId, request.getAmount());
        }
        
        return TransactionResponse.from(updatedTransaction);
    }
    
    /**
     * 거래 내역 삭제
     */
    @Transactional
    public void deleteTransaction(Long transactionId, Long coupleId) {
        // 거래 내역 조회
        FinanceTransaction transaction = financeTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new FinanceTransactionNotFoundException("거래 내역을 찾을 수 없습니다."));
        
        // 권한 확인 (같은 커플인지)
        if (!transaction.getCoupleId().equals(coupleId)) {
            throw new IllegalArgumentException("해당 거래 내역에 대한 권한이 없습니다.");
        }
        
        // 지출인 경우에만 커플의 current_spent와 카테고리별 예산에서 차감
        if (transaction.getTransactionType() == FinanceTransaction.TransactionType.EXPENSE) {
            coupleService.decreaseCurrentSpent(coupleId, transaction.getAmount().abs());
            
            // 카테고리별 예산 업데이트 (예산이 설정된 경우에만)
            try {
                if (categoryBudgetService.existsByCategoryId(transaction.getFinanceCategory().getFinanceCategoryId())) {
                    categoryBudgetService.decreaseSpentAmount(
                            transaction.getFinanceCategory().getFinanceCategoryId(),
                            transaction.getAmount().abs()
                    );
                }
            } catch (Exception e) {
                log.warn("거래 삭제 시 카테고리 예산 차감 중 오류가 발생했습니다. 카테고리 ID: {}, 오류: {}", 
                        transaction.getFinanceCategory().getFinanceCategoryId(), e.getMessage());
            }
        }
        
        // 거래 내역 삭제
        financeTransactionRepository.delete(transaction);
    }
    
    /**
     * 거래 내역 단건 조회
     */
    public TransactionResponse getTransaction(Long transactionId, Long coupleId) {
        FinanceTransaction transaction = financeTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new FinanceTransactionNotFoundException("거래 내역을 찾을 수 없습니다: " + transactionId));
        
        // 권한 확인 (같은 커플인지)
        if (!transaction.getCoupleId().equals(coupleId)) {
            throw new FinanceTransactionNotFoundException("해당 커플의 거래 내역이 아닙니다: " + transactionId);
        }
        
        return TransactionResponse.from(transaction);
    }
    
    /**
     * 거래 내역 목록 조회 (검색 조건 포함, 사용자 이름 포함)
     */
    public Page<TransactionResponse> getTransactions(TransactionSearchRequest request, Long coupleId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        
        Page<Object[]> results = financeTransactionRepository.findBySearchConditionsWithUserName(
                coupleId,
                request.getCategoryId(),
                request.getUserSeqNo(),
                request.getTransactionType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getSearchTerm(),
                pageable
        );
        
        return results.map(this::convertToResponseWithUserName);
    }
    

    
    /**
     * 카테고리별 총 지출 금액 조회
     */
    public Map<Long, BigDecimal> getTotalSpentByCategory(Long coupleId) {
        List<Object[]> results = financeTransactionRepository.findTotalSpentByCategory(coupleId);
        
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (BigDecimal) result[1]
                ));
    }
    
    /**
     * JOIN 결과를 TransactionResponse로 변환
     */
    private TransactionResponse convertToResponseWithUserName(Object[] result) {
        FinanceTransaction transaction = (FinanceTransaction) result[0];
        String userName = (String) result[1];
        String categoryName = (String) result[2];
        String iconName = (String) result[3];
        String colorName = (String) result[4];
        
        return TransactionResponse.builder()
                .transactionId(transaction.getFinanceTransactionId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount().abs())
                .transactionType(transaction.getTransactionType())
                .transactionDate(transaction.getTransactionDate())
                .transactionTime(transaction.getTransactionTime())
                .memo(transaction.getMemo())
                .categoryId(transaction.getFinanceCategory().getFinanceCategoryId())
                .categoryName(categoryName)
                .iconName(iconName)
                .colorName(colorName)
                .userSeqNo(transaction.getUserSeqNo())
                .userName(userName)
                .createdAt(transaction.getCreatedAt().toString())
                .updatedAt(transaction.getUpdatedAt().toString())
                .build();
    }
    
    /**
     * CategoryResponse로 변환
     */
    private com.marry1q.marry1qbe.domain.finance.dto.response.CategoryResponse convertToCategoryResponse(FinanceCategory category) {
        return com.marry1q.marry1qbe.domain.finance.dto.response.CategoryResponse.builder()
                .financeCategoryId(category.getFinanceCategoryId())
                .name(category.getName())
                .coupleId(category.getCoupleId())
                .build();
    }
}
