package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import com.wanted.codebombalms.learning.application.port.LearningUserPort;
import com.wanted.codebombalms.learning.application.usecase.AdminLearningProgressQueryUseCase;
import com.wanted.codebombalms.learning.domain.model.StudentLearningProgress;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLearningProgressQueryService implements AdminLearningProgressQueryUseCase {

    private final LearningEnrollmentPort learningEnrollmentPort;
    private final LearningLecturePort learningLecturePort;
    private final LearningCourseProblemPort learningCourseProblemPort;
    private final LearningUserPort learningUserPort;
    private final LectureProgressRepository lectureProgressRepository;
    private final LectureProblemProgressRepository lectureProblemProgressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StudentLearningProgress> findStudentProgresses(Long courseId) {
        List<Long> studentIds = learningEnrollmentPort.findActiveStudentIdsByCourse(courseId);
        List<Long> lectureIds = learningLecturePort.findLectureIdsByCourse(courseId);
        List<Long> courseProblemStepIds = learningCourseProblemPort.findCourseProblemStepIdsByCourse(courseId);

        return studentIds.stream()
                .map(studentId -> StudentLearningProgress.of(
                        studentId,
                        learningUserPort.findUserName(studentId),
                        lectureProgressRepository.countCompletedByUserIdAndLectureIds(studentId, lectureIds),
                        lectureIds.size(),
                        lectureProblemProgressRepository.countCompletedByUserIdAndCourseProblemStepIds(
                                studentId,
                                courseProblemStepIds
                        ),
                        courseProblemStepIds.size()
                ))
                .toList();
    }
}
