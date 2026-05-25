package com.wanted.codebombalms.admin.operation.alert.infrastructure.cleanup;

import com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence.SpringDataOperationAlertRepository;
import com.wanted.codebombalms.global.application.cleanup.DefaultHardDeleteTarget;
import com.wanted.codebombalms.global.application.cleanup.port.HardDeleteTarget;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Period;

@Configuration
// 운영 알림 하드 딜리트 대상을 공통 cleanup 실행기에 등록한다.
public class OperationAlertCleanupConfig {

    // deleted_at 기준으로 6개월이 지난 운영 알림을 하드 딜리트하도록 설정한다.
    @Bean
    public HardDeleteTarget operationAlertHardDeleteTarget(
            SpringDataOperationAlertRepository repository
    ) {
        return new DefaultHardDeleteTarget(
                "operation-alert",
                Period.ofMonths(6),
                repository::hardDeleteByDeletedAtBefore
        );
    }
}
