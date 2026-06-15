package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.lecture.application.command.ConfigureLectureProblemSetsCommand;
import com.wanted.codebombalms.lecture.application.policy.LectureProblemSetPolicy;
import com.wanted.codebombalms.lecture.application.port.CourseCatalogPort;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetCommandUseCase;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.repository.LectureProblemSetRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Map<ProblemSetKey, Long> existingIds = new HashMap<>();
        for (LectureProblemSet existingProblemSet : existingProblemSets) {
            existingIds.putIfAbsent(
                    new ProblemSetKey(existingProblemSet.getLectureId(), existingProblemSet.getProblemSetId()),
                    existingProblemSet.getLectureProblemSetId()
            );
        }

        for (ConfigureLectureProblemSetsCommand.ProblemSetCommand problemSetCommand : command.problemSets()) {
            Long existingId = existingIds.get(
                    new ProblemSetKey(problemSetCommand.lectureId(), problemSetCommand.problemSetId())
            );
            LectureProblemSet lectureProblemSet = existingId == null
                    ? LectureProblemSet.create(
                            command.courseId(),
                            problemSetCommand.lectureId(),
                            problemSetCommand.problemSetId(),
                            problemSetCommand.role(),
                            problemSetCommand.displayOrder()
                    )
                    : LectureProblemSet.restore(
                            existingId,
                            command.courseId(),
                            problemSetCommand.lectureId(),
                            problemSetCommand.problemSetId(),
                            problemSetCommand.role(),
                            problemSetCommand.displayOrder()
                    );

            lectureProblemSetRepository.save(lectureProblemSet);
        }

        return lectureProblemSetRepository.findByCourseId(command.courseId());
    }

    private record ProblemSetKey(Long lectureId, Long problemSetId) {
    }
}
