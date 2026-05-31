package com.wanted.codebombalms.admin.operation.automation.infrastructure.adapter;

import com.wanted.codebombalms.admin.operation.automation.application.model.OperationRuleDetectionResult;
import com.wanted.codebombalms.admin.operation.automation.application.port.CourseOperationMetricPort;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 강좌와 수강신청 데이터를 조회해 수강생 부족 강좌 지표를 만든다.
public class CourseOperationMetricAdapter implements CourseOperationMetricPort {

    private final CourseQueryUseCase courseQueryUseCase;
    private final EnrollmentQueryUseCase enrollmentQueryUseCase;

    @Override
    public List<OperationRuleDetectionResult> findLowEnrollmentCourses(BigDecimal threshold) {
        long maxEnrollmentCount = threshold.longValue();
        Map<Long, Long> enrollmentCountsByCourseId = enrollmentQueryUseCase.findAllActiveEnrollments()
                .stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                .collect(Collectors.groupingBy(
                        enrollment -> enrollment.getCourseId(),
                        Collectors.counting()
                ));

        return courseQueryUseCase.findAllCourses(null)
                .stream()
                .filter(course -> course.getStatus() == CourseStatus.ACTIVE)
                .map(course -> new CourseEnrollmentMetric(
                        course.getCourseId(),
                        enrollmentCountsByCourseId.getOrDefault(course.getCourseId(), 0L)
                ))
                .filter(metric -> metric.enrollmentCount() <= maxEnrollmentCount)
                .map(metric -> toResult(metric.courseId(), metric.enrollmentCount(), threshold))
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

    private record CourseEnrollmentMetric(Long courseId, Long enrollmentCount) {
    }
}
