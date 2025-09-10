package com.marry1q.marry1qbe.grobal.commonCode.repository;

import com.marry1q.marry1qbe.grobal.commonCode.entity.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommonCodeRepository extends JpaRepository<CommonCode, String> {
    
    List<CommonCode> findByCodeGroupAndIsActiveTrueOrderBySortOrderAsc(String codeGroup);
    
    Optional<CommonCode> findByCodeIdAndIsActiveTrue(String codeId);
    
    boolean existsByCodeIdAndIsActiveTrue(String codeId);
    
    /**
     * 코드 그룹, 코드값, 활성화 여부로 특정 코드 조회
     */
    Optional<CommonCode> findByCodeGroupAndCodeValueAndIsActiveTrue(String codeGroup, String codeValue);
    
    /**
     * 코드 그룹으로 모든 코드 목록 조회 (활성화 여부 상관없이)
     */
    List<CommonCode> findByCodeGroupOrderBySortOrderAsc(String codeGroup);
}
