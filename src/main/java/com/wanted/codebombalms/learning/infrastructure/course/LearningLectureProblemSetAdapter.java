package com.wanted.codebombalms.learning.infrastructure.course;

import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.repository.CourseProblemSetRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSet;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSetPort;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningLectureProblemSetAdapter implements LearningLectureProblemSetPort {

    private final CourseProblemSetRepository courseProblemSetRepository;
    private final SpringDataProblemRepository problemRepository;

    @Override
    public LearningLectureProblemSet findLectureProblemSet(Long lectureProblemSetId) {
        CourseProblemSet lectureProblemSet = courseProblemSetRepository.findById(lectureProblemSetId)
                .orElseThrow(() -> new NotFoundException(LearningErrorCode.LECTURE_PROBLEM_SET_NOT_FOUND));

        return new LearningLectureProblemSet(
                lectureProblemSet.getCourseProblemSetId(),
                lectureProblemSet.getLectureId(),
                lectureProblemSet.getProblemSetId()
        );
    }

    @Override
    public boolean existsProblemInSet(Long problemSetId, Long problemId) {
        return problemRepository.findByProblemIdAndProblemSet_ProblemSetId(problemId, problemSetId)
                .isPresent();
    }
}
