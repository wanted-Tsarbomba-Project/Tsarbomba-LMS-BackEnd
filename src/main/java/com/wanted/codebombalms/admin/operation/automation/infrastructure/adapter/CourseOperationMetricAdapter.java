package com.wanted.codebombalms.admin.operation.automation.infrastructure.adapter;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.port.CourseOperationMetricPort;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 강좌와 수강신청 데이터를 조회해 수강생 부족 강좌 지표를 만든다.
public class CourseOperationMetricAdapter implements CourseOperationMetricPort {

    private final EntityManager entityManager;

    @Override
    public List<OperationRuleDetectionResult> findLowEnrollmentCourses(BigDecimal threshold) {
        long maxEnrollmentCount = threshold.longValue();

        return entityManager.createQuery("""
                        select c.courseId, count(e.enrollmentId)
                        from CourseJpaEntity c
                        left join EnrollmentJpaEntity e
                            on e.course = c
                            and e.status = :activeEnrollmentStatus
                        where c.deletedAt is null
                          and c.status = :activeCourseStatus
                        group by c.courseId
                        having count(e.enrollmentId) <= :maxEnrollmentCount
                        """, Object[].class)
                .setParameter("activeEnrollmentStatus", EnrollmentStatus.ACTIVE)
                .setParameter("activeCourseStatus", CourseStatus.ACTIVE)
                .setParameter("maxEnrollmentCount", maxEnrollmentCount)
                .getResultList()
                .stream()
                .map(row -> toResult((Long) row[0], (Long) row[1], threshold))
                .toList();
    }

    private OperationRuleDetectionResult toResult(
            Long courseId,
            Long enrollmentCount,
            BigDecimal threshold
    ) {
        return new OperationRuleDetectionResult(
                OperationTargetType.COURSE,
                courseId,
                BigDecimal.valueOf(enrollmentCount),
                "수강생 수가 기준 이하입니다. 현재 수강생 수: " + enrollmentCount + "명",
                "강좌 노출, 홍보 상태, 커리큘럼 구성을 점검하세요. 기준값: " + threshold + "명"
        );
    }
}
