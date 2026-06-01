package com.wanted.codebombalms.course.infrastructure.cleanup;

import com.wanted.codebombalms.course.infrastructure.persistence.SpringDataCourseRepository;
import com.wanted.codebombalms.global.application.cleanup.DefaultHardDeleteTarget;
import com.wanted.codebombalms.global.application.cleanup.port.HardDeleteTarget;
import java.time.Period;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class CourseCleanupConfig {

    @Bean
    @Order(30)
    public HardDeleteTarget courseHardDeleteTarget(
            SpringDataCourseRepository repository
    ) {
        return new DefaultHardDeleteTarget(
                "course",
                Period.ofMonths(6),
                repository::hardDeleteByDeletedAtBefore
        );
    }
}
