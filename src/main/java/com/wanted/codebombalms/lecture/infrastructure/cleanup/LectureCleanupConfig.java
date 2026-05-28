package com.wanted.codebombalms.lecture.infrastructure.cleanup;

import com.wanted.codebombalms.global.application.cleanup.DefaultHardDeleteTarget;
import com.wanted.codebombalms.global.application.cleanup.port.HardDeleteTarget;
import com.wanted.codebombalms.lecture.infrastructure.persistence.SpringDataLectureRepository;
import java.time.Period;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class LectureCleanupConfig {

    @Bean
    @Order(20)
    public HardDeleteTarget lectureHardDeleteTarget(
            SpringDataLectureRepository repository
    ) {
        return new DefaultHardDeleteTarget(
                "lecture",
                Period.ofMonths(6),
                repository::hardDeleteByDeletedAtBefore
        );
    }
}
