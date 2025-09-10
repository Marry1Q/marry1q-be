package com.marry1q.marry1qbe.domain.invitation.repository;

import com.marry1q.marry1qbe.domain.invitation.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    
    // 커플 ID와 대표 청첩장 여부로 조회
    Optional<Invitation> findByCoupleIdAndIsRepresentativeTrue(Long coupleId);
    
    // 커플 ID로 최근 수정된 청첩장 조회
    Optional<Invitation> findTopByCoupleIdOrderByUpdatedAtDesc(Long coupleId);
    
    // 커플 ID로 모든 청첩장 조회 (최신순)
    List<Invitation> findByCoupleIdOrderByUpdatedAtDesc(Long coupleId);
    
    // 커플 ID로 대표 청첩장 개수 조회
    long countByCoupleIdAndIsRepresentativeTrue(Long coupleId);
}
