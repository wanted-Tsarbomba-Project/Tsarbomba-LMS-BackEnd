package com.wanted.codebombalms.enrollment.infrastructure.learning;

import com.wanted.codebombalms.enrollment.application.port.EnrollmentLearningProgressPort;
import com.wanted.codebombalms.enrollment.application.port.EnrollmentLearningProgressPort.EnrollmentLearningProgress;
import com.wanted.codebombalms.enrollment.application.port.EnrollmentLearningProgressPort.EnrollmentLearningProgressKey;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository.UserCourseKey;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import com.wanted.codebombalms.lecture.domain.repository.LectureProblemSetRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentLearningProgressAdapter implements EnrollmentLearningProgressPort {

    private final LectureRepository lectureRepository;
    private final LectureProblemSetRepository lectureProblemSetRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final LectureProblemProgressRepository lectureProblemProgressRepository;

    @Override
    public Map<EnrollmentLearningProgressKey, EnrollmentLearningProgress> findProgresses(
            Collection<EnrollmentLearningProgressKey> keys
    ) {
        if (keys.isEmpty()) {
            return Map.of();
        }

        Set<Long> userIds = keys.stream()
                .map(EnrollmentLearningProgressKey::userId)
                .collect(Collectors.toSet());
        Set<Long> courseIds = keys.stream()
                .map(EnrollmentLearningProgressKey::courseId)
                .collect(Collectors.toSet());

        Map<Long, Long> totalLectureCounts = lectureRepository.countActiveByCourseIds(courseIds);
        Map<Long, Long> totalProblemCounts = lectureProblemSetRepository.countActiveByCourseIdsAndRole(
                courseIds,
                LectureProblemSetRole.MAIN
        );
        Map<UserCourseKey, Long> completedLectureCounts =
                lectureProgressRepository.countCompletedByUserIdsAndCourseIds(userIds, courseIds);
        Map<UserCourseKey, Long> completedProblemCounts =
                lectureProblemProgressRepository.countCompletedMainByUserIdsAndCourseIds(userIds, courseIds);

        return keys.stream()
                .distinct()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> {
                            UserCourseKey userCourseKey = new UserCourseKey(key.userId(), key.courseId());
                            return EnrollmentLearningProgress.of(
                                    completedLectureCounts.getOrDefault(userCourseKey, 0L),
                                    totalLectureCounts.getOrDefault(key.courseId(), 0L),
                                    completedProblemCounts.getOrDefault(userCourseKey, 0L),
                                    totalProblemCounts.getOrDefault(key.courseId(), 0L)
                            );
                        }
                ));
    }
}
