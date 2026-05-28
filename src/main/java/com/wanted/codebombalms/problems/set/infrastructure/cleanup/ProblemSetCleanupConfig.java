package com.wanted.codebombalms.problems.set.infrastructure.cleanup;

import com.wanted.codebombalms.global.application.cleanup.DefaultHardDeleteTarget;
import com.wanted.codebombalms.global.application.cleanup.port.HardDeleteTarget;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import java.time.Period;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProblemSetCleanupConfig {

    @Bean
    public HardDeleteTarget problemSetHardDeleteTarget(
            SpringDataProblemSetRepository repository
    ) {
        return new DefaultHardDeleteTarget(
                "problem-set",
                Period.ofMonths(3),
                repository::hardDeleteByDeletedAtBefore
        );
    }
}