package com.wanted.codebombalms.enrollment.infrastructure.learning;

import com.wanted.codebombalms.enrollment.application.port.EnrollmentLearningProgressPort;
import com.wanted.codebombalms.enrollment.application.port.EnrollmentLearningProgressPort.EnrollmentLearningProgress;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetQueryUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentLearningProgressAdapter implements EnrollmentLearningProgressPort {

    private final LectureQueryUseCase lectureQueryUseCase;
    private final LectureProblemSetQueryUseCase lectureProblemSetQueryUseCase;
    private final LectureProgressRepository lectureProgressRepository;
    private final LectureProblemProgressRepository lectureProblemProgressRepository;

    @Override
    public EnrollmentLearningProgress findProgress(Long userId, Long courseId) {
        List<Long> lectureIds = lectureQueryUseCase.findLecturesByCourseId(courseId)
                .stream()
                .map(Lecture::getLectureId)
                .toList();
        List<Long> lectureProblemSetIds = lectureProblemSetQueryUseCase.findProblemSetsByCourseAndRole(
                        courseId,
                        LectureProblemSetRole.MAIN
                )
                .stream()
                .map(LectureProblemSet::getLectureProblemSetId)
                .toList();

        long completedLectureCount = lectureProgressRepository.countCompletedByUserIdAndLectureIds(
                userId,
                lectureIds
        );
        long completedProblemCount = lectureProblemProgressRepository.countCompletedByUserIdAndLectureProblemSetIds(
                userId,
                lectureProblemSetIds
        );

        return EnrollmentLearningProgress.of(
                completedLectureCount,
                lectureIds.size(),
                completedProblemCount,
                lectureProblemSetIds.size()
        );
    }
}
