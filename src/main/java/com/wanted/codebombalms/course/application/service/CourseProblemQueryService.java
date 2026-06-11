package com.wanted.codebombalms.course.application.service;

import com.wanted.codebombalms.course.application.usecase.CourseProblemQueryUseCase;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.course.domain.repository.CourseProblemSetRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseProblemQueryService implements CourseProblemQueryUseCase {

    private final CourseProblemSetRepository courseProblemSetRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseProblemSet> findProblemSetsByCourse(Long courseId) {
        return courseProblemSetRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseProblemSet> findProblemSetsByCourseAndRole(Long courseId, CourseProblemSetRole role) {
        return courseProblemSetRepository.findByCourseIdAndRole(courseId, role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseProblemSet> findProblemSetsByLecture(Long lectureId) {
        return courseProblemSetRepository.findByLectureId(lectureId);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProblemSet findProblemSetById(Long courseProblemSetId) {
        return courseProblemSetRepository.findById(courseProblemSetId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_PROBLEM_SET_NOT_FOUND));
    }
}
