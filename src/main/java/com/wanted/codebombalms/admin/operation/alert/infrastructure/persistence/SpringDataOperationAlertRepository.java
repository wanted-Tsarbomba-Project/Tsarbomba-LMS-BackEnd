package com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataOperationAlertRepository
        extends JpaRepository<OperationAlertJpaEntity, Long> {

    @Query(
            value = """
                    select new com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence.OperationAlertWithRuleProjection(
                        oa.operationAlertId,
                        oa.operationRuleId,
                        oa.targetType,
                        oa.targetId,
                        oa.detectedValue,
                        oa.thresholdValueSnapshot,
                        ar.severity,
                        oa.status,
                        oa.assigneeId,
                        oa.reason,
                        oa.recommendedAction,
                        oa.firstDetectedAt,
                        oa.lastDetectedAt
                    )
                    from OperationAlertJpaEntity oa
                    join AutomationRuleJpaEntity ar
                        on ar.operationRuleId = oa.operationRuleId
                    where oa.deletedAt is null
                      and (:targetType is null or oa.targetType = :targetType)
                      and (:status is null or oa.status = :status)
                    """,
            countQuery = """
                    select count(oa)
                    from OperationAlertJpaEntity oa
                    join AutomationRuleJpaEntity ar
                        on ar.operationRuleId = oa.operationRuleId
                    where oa.deletedAt is null
                      and (:targetType is null or oa.targetType = :targetType)
                      and (:status is null or oa.status = :status)
                    """
    )
    Page<OperationAlertWithRuleProjection> findAlerts(
            @Param("targetType") OperationTargetType targetType,
            @Param("status") OperationAlertStatus status,
            Pageable pageable
    );

    Optional<OperationAlertJpaEntity> findByOperationAlertIdAndDeletedAtIsNull(Long operationAlertId);
}
