package com.wanted.codebombalms.serviceevent.infrastructure.scheduler;

import com.wanted.codebombalms.serviceevent.application.service.BriefingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AI 브리핑 스케줄 생성 (#609) — 하루 3회.
 * 09:05 는 기존 09:00 잡(OperationRuleScheduler)과 5분 비껴 건다.
 * 실패는 BriefingService 가 FAILED 행으로 기록하므로 여기선 트리거만 담당.
 */
@Component
@RequiredArgsConstructor
public class BriefingScheduler {

    private final BriefingService briefingService;

    @Scheduled(cron = "0 5 9 * * *")
    public void generateMorning() {
        briefingService.generateScheduled();
    }

    @Scheduled(cron = "0 0 15 * * *")
    public void generateAfternoon() {
        briefingService.generateScheduled();
    }

    @Scheduled(cron = "0 0 21 * * *")
    public void generateEvening() {
        briefingService.generateScheduled();
    }
}
