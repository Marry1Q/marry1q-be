package com.marry1q.marry1qbe.domain.plan1q.service;

import com.marry1q.marry1qbe.domain.plan1q.entity.Plan1QGoal;
import com.marry1q.marry1qbe.domain.plan1q.repository.Plan1QGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class Plan1QGoalLifecycleService {

    private final Plan1QGoalRepository plan1QGoalRepository;

    /**
     * 매일 02:00에 만기일이 지난 운용중(in_progress) 목표를 completed로 전환
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void completeMaturedGoals() {
        LocalDate today = LocalDate.now();
        log.info("⏰ 만기 목표 전환 배치 시작 - 기준일: {}", today);

        List<Plan1QGoal> targets = plan1QGoalRepository.findByMaturityDateBeforeAndStatus(today.plusDays(1), "in_progress");
        log.info("🔎 전환 대상 목표 수: {}", targets.size());

        for (Plan1QGoal goal : targets) {
            goal.setStatus("completed");
        }

        log.info("✅ 만기 목표 전환 배치 완료");
    }
}


