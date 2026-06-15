package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.lecture.application.command.ConfigureLectureProblemSetsCommand;
import com.wanted.codebombalms.lecture.application.policy.LectureProblemSetPolicy;
import com.wanted.codebombalms.lecture.application.port.CourseCatalogPort;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetCommandUseCase;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.repository.LectureProblemSetRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureProblemSetCommandService implements LectureProblemSetCommandUseCase {

    private final CourseCatalogPort courseCatalogPort;
    private final LectureProblemSetRepository lectureProblemSetRepository;
    private final LectureProblemSetPolicy lectureProblemSetPolicy;

    @Override
    public List<LectureProblemSet> configureProblemSets(ConfigureLectureProblemSetsCommand command) {
        courseCatalogPort.findCourse(command.courseId());

        lectureProblemSetPolicy.validate(command);

        List<LectureProblemSet> existingProblemSets = lectureProblemSetRepository.findByCourseId(command.courseId());

        for (ConfigureLectureProblemSetsCommand.ProblemSetCommand problemSetCommand : command.problemSets()) {
            Long existingId = existingProblemSets.stream()
                    .filter(problemSet -> Objects.equals(problemSet.getLectureId(), problemSetCommand.lectureId()))
                    .filter(problemSet -> problemSet.getProblemSetId().equals(problemSetCommand.problemSetId()))
                    .map(LectureProblemSet::getLectureProblemSetId)
                    .findFirst()
                    .orElse(null);

            lectureProblemSetRepository.save(LectureProblemSet.restore(
                    existingId,
                    command.courseId(),
                    problemSetCommand.lectureId(),
                    problemSetCommand.problemSetId(),
                    problemSetCommand.role(),
                    problemSetCommand.displayOrder()
            ));
        }

        return lectureProblemSetRepository.findByCourseId(command.courseId());
    }
}
