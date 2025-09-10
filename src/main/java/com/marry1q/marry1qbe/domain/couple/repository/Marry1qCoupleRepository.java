package com.marry1q.marry1qbe.domain.couple.repository;

import com.marry1q.marry1qbe.domain.couple.entity.Marry1qCouple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Marry1qCoupleRepository extends JpaRepository<Marry1qCouple, Long> {
}
