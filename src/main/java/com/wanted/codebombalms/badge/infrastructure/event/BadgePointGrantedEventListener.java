package com.wanted.codebombalms.badge.infrastructure.event;

import com.wanted.codebombalms.badge.application.usecase.SyncUserBadgesUseCase;
import com.wanted.codebombalms.reward.point.domain.event.PointGrantedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BadgePointGrantedEventListener {

    private final SyncUserBadgesUseCase syncUserBadgesUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PointGrantedEvent event) {
        try {
            var result = syncUserBadgesUseCase.sync(
                    event.userId(),
                    event.totalPoint()
            );

            log.info(
                    "포인트 지급 후 뱃지 동기화 완료. userId={}, newlyEarnedBadgeCount={}",
                    event.userId(),
                    result.newlyEarnedBadgeCount()
            );
        } catch (Exception e) {
            log.error(
                    "포인트 지급 후 뱃지 동기화 실패. userId={}, totalPoint={}",
                    event.userId(),
                    event.totalPoint(),
                    e
            );
        }
    }
}
