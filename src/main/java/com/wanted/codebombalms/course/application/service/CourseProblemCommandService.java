package com.wanted.codebombalms.course.application.service;

import com.wanted.codebombalms.course.application.command.ConfigureCourseProblemSetsCommand;
import com.wanted.codebombalms.course.application.policy.CourseProblemPolicy;
import com.wanted.codebombalms.course.application.usecase.CourseProblemCommandUseCase;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemStep;
import com.wanted.codebombalms.course.domain.repository.CourseProblemSetRepository;
import com.wanted.codebombalms.course.domain.repository.CourseProblemStepRepository;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseProblemCommandService implements CourseProblemCommandUseCase {

    private final CourseRepository courseRepository;
    private final CourseProblemSetRepository courseProblemSetRepository;
    private final CourseProblemStepRepository courseProblemStepRepository;
    private final CourseProblemPolicy courseProblemPolicy;

    @Override
    public List<CourseProblemSet> configureProblemSets(ConfigureCourseProblemSetsCommand command) {
        courseRepository.findByCourseIdAndDeletedAtIsNull(command.courseId())
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        courseProblemPolicy.validate(command);

        List<CourseProblemSet> existingProblemSets = courseProblemSetRepository.findByCourseId(command.courseId());
        existingProblemSets.forEach(problemSet ->
                courseProblemStepRepository.deleteByCourseProblemSetId(problemSet.getCourseProblemSetId())
        );
        courseProblemSetRepository.deleteByCourseId(command.courseId());

        for (ConfigureCourseProblemSetsCommand.ProblemSetCommand problemSetCommand : command.problemSets()) {
            CourseProblemSet savedProblemSet = courseProblemSetRepository.save(CourseProblemSet.create(
                    command.courseId(),
                    problemSetCommand.problemSetId(),
                    problemSetCommand.role()
            ));

            for (ConfigureCourseProblemSetsCommand.ProblemStepCommand stepCommand : problemSetCommand.steps()) {
                courseProblemStepRepository.save(CourseProblemStep.create(
                        savedProblemSet.getCourseProblemSetId(),
                        stepCommand.problemId(),
                        stepCommand.lectureId(),
                        stepCommand.stepOrder()
                ));
            }
        }

        return courseProblemSetRepository.findByCourseId(command.courseId());
    }
}
