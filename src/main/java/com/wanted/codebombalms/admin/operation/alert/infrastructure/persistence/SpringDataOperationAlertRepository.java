package com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SpringDataOperationAlertRepository
        extends JpaRepository<OperationAlertJpaEntity, Long> {

    @Query(
            value = """
                    select new com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence.OperationAlertWithRuleProjection(
                        oa.operationAlertId,
                        oa.status,
                        oa.recommendedAction
                    )
                    from OperationAlertJpaEntity oa
                    where oa.deletedAt is null
                      and (:targetType is null or oa.targetType = :targetType)
                      and (:status is null or oa.status = :status)
                    """,
            countQuery = """
                    select count(oa)
                    from OperationAlertJpaEntity oa
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

    // 알림 ID로 삭제되지 않은 알림과 연결된 자동 규칙 정보를 함께 조회한다.
    @Query("""
            select new com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence.OperationAlertDetailProjection(
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
                oa.lastDetectedAt,
                oa.resolvedBy,
                oa.resolvedAt,
                oa.adminMemo,
                oa.createdAt,
                oa.updatedAt,
                ar.ruleCode,
                ar.minSampleCount
            )
            from OperationAlertJpaEntity oa
            join AutomationRuleJpaEntity ar
                on ar.operationRuleId = oa.operationRuleId
            where oa.operationAlertId = :operationAlertId
              and oa.deletedAt is null
            """)
    Optional<OperationAlertDetailProjection> findAlertDetail(@Param("operationAlertId") Long operationAlertId);

    Optional<OperationAlertJpaEntity> findByOperationAlertIdAndDeletedAtIsNull(Long operationAlertId);

    // 같은 규칙과 대상에 대해 삭제되지 않은 특정 상태의 알림을 조회한다.
    Optional<OperationAlertJpaEntity> findByOperationRuleIdAndTargetTypeAndTargetIdAndStatusAndDeletedAtIsNull(
            Long operationRuleId,
            OperationTargetType targetType,
            Long targetId,
            OperationAlertStatus status
    );

    // 소프트 딜리트 시각이 기준 시각보다 오래된 운영 알림을 하드 딜리트한다.
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from OperationAlertJpaEntity oa
            where oa.deletedAt is not null
              and oa.deletedAt < :threshold
            """)
    int hardDeleteByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);
}
