package com.wanted.codebombalms.badge.infrastructure.cleanup;

import com.wanted.codebombalms.badge.application.usecase.HardDeleteBadgesUseCase;
import com.wanted.codebombalms.global.application.cleanup.DefaultHardDeleteTarget;
import com.wanted.codebombalms.global.application.cleanup.port.HardDeleteTarget;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Period;

@Configuration
public class BadgeCleanupConfig {

    @Bean
    public HardDeleteTarget badgeHardDeleteTarget(
            HardDeleteBadgesUseCase hardDeleteBadgesUseCase
    ) {
        return new DefaultHardDeleteTarget(
                "badge",
                Period.ofMonths(3),
                hardDeleteBadgesUseCase::hardDeleteBefore
        );
    }
}
