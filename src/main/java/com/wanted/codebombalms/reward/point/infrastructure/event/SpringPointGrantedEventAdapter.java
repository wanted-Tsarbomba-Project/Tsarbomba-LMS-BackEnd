package com.wanted.codebombalms.reward.point.infrastructure.event;

import com.wanted.codebombalms.reward.point.application.port.PointGrantedEventPort;
import com.wanted.codebombalms.reward.point.domain.event.PointGrantedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringPointGrantedEventAdapter
        implements PointGrantedEventPort {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(Long userId, Integer totalPoint) {
        eventPublisher.publishEvent(
                new PointGrantedEvent(userId, totalPoint)
        );
    }
}
