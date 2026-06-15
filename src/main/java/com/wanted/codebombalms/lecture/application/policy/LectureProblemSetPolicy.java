package com.wanted.codebombalms.lecture.application.policy;

import com.wanted.codebombalms.lecture.application.command.ConfigureLectureProblemSetsCommand;
import com.wanted.codebombalms.lecture.application.port.ProblemSetCatalogPort;
import com.wanted.codebombalms.lecture.domain.exception.LectureProblemSetErrorCode;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureProblemSetPolicy {

    private final ProblemSetCatalogPort problemSetCatalogPort;
    private final LectureRepository lectureRepository;

    public void validate(ConfigureLectureProblemSetsCommand command) {
        if (command.problemSets() == null || command.problemSets().isEmpty()) {
            throw new ValidationException(LectureProblemSetErrorCode.PROBLEM_SET_REQUIRED);
        }

        Set<ProblemSetKey> problemSetKeys = new HashSet<>();
        for (ConfigureLectureProblemSetsCommand.ProblemSetCommand problemSet : command.problemSets()) {
            if (problemSet == null) {
                throw new ValidationException(LectureProblemSetErrorCode.PROBLEM_SET_REQUIRED);
            }
            validateProblemSet(command.courseId(), problemSet);
            if (!problemSetKeys.add(new ProblemSetKey(problemSet.lectureId(), problemSet.problemSetId()))) {
                throw new ValidationException(LectureProblemSetErrorCode.DUPLICATE_PROBLEM_SET);
            }
        }
    }

    private void validateProblemSet(
            Long courseId,
            ConfigureLectureProblemSetsCommand.ProblemSetCommand problemSet
    ) {
        if (problemSet.problemSetId() == null || problemSet.role() == null) {
            throw new ValidationException(LectureProblemSetErrorCode.PROBLEM_SET_REQUIRED);
        }
        if (problemSet.displayOrder() == null || problemSet.displayOrder() < 1) {
            throw new ValidationException(LectureProblemSetErrorCode.PROBLEM_STEP_REQUIRED);
        }
        if (!problemSetCatalogPort.existsProblemSet(problemSet.problemSetId())) {
            throw new ValidationException(LectureProblemSetErrorCode.PROBLEM_SET_NOT_FOUND);
        }
        if (problemSet.role() == LectureProblemSetRole.FINAL) {
            if (problemSet.lectureId() != null) {
                throw new ValidationException(LectureProblemSetErrorCode.FINAL_LECTURE_NOT_ALLOWED);
            }
            return;
        }
        if (problemSet.lectureId() == null) {
            throw new ValidationException(LectureProblemSetErrorCode.MAIN_LECTURE_REQUIRED);
        }
        if (!lectureRepository.existsByCourseIdAndLectureIdAndDeletedAtIsNull(courseId, problemSet.lectureId())) {
            throw new ValidationException(LectureProblemSetErrorCode.LECTURE_NOT_IN_COURSE);
        }
    }

    private record ProblemSetKey(Long lectureId, Long problemSetId) {
    }
}
