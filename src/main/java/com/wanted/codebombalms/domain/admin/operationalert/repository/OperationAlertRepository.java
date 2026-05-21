//package com.wanted.codebombalms.domain.admin.operationalert.repository;
//
//import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationTargetType;
//import com.wanted.codebombalms.domain.admin.operationalert.entity.OperationAlert;
//import com.wanted.codebombalms.domain.admin.operationalert.enums.OperationAlertStatus;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//public interface OperationAlertRepository extends JpaRepository<OperationAlert, Long> {
//
//    @Query(
//            value = """
//                    select oa
//                    from OperationAlert oa
//                    join fetch oa.automationRule ar
//                    where (:targetType is null or oa.targetType = :targetType)
//                      and (:status is null or oa.status = :status)
//                    """,
//            countQuery = """
//                    select count(oa)
//                    from OperationAlert oa
//                    where (:targetType is null or oa.targetType = :targetType)
//                      and (:status is null or oa.status = :status)
//                    """
//    )
//    Page<OperationAlert> findOperationAlerts(
//            @Param("targetType") OperationTargetType targetType,
//            @Param("status") OperationAlertStatus status,
//            Pageable pageable
//    );
//}