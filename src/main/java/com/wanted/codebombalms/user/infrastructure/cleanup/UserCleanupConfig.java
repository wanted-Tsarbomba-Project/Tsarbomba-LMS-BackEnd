package com.wanted.codebombalms.user.infrastructure.cleanup;

import com.wanted.codebombalms.global.application.cleanup.DefaultHardDeleteTarget;
import com.wanted.codebombalms.global.application.cleanup.port.HardDeleteTarget;
import com.wanted.codebombalms.user.application.usecase.HardDeleteUsersUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Period;

@Configuration
public class UserCleanupConfig {

    @Bean
    public HardDeleteTarget userHardDeleteTarget(
            HardDeleteUsersUseCase hardDeleteUsersUseCase
    ) {
        return new DefaultHardDeleteTarget(
                "user",
                Period.ofYears(1),                       // 탈퇴 후 1년 보관 → 파기
                hardDeleteUsersUseCase::hardDeleteBefore
        );
    }
}
