package com.wanted.codebombalms.lecture.application.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.lecture.application.command.ConfigureLectureProblemSetsCommand;
import com.wanted.codebombalms.lecture.application.port.ProblemSetCatalogPort;
import com.wanted.codebombalms.lecture.domain.exception.LectureProblemSetErrorCode;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LectureProblemSetPolicyTest {

    @Mock
    private ProblemSetCatalogPort problemSetCatalogPort;

    @Mock
    private LectureRepository lectureRepository;

    @InjectMocks
    private LectureProblemSetPolicy lectureProblemSetPolicy;

    @Test
    void validate_allowsMainProblemSetForLectureInCourse() {
        ConfigureLectureProblemSetsCommand command = command(10L, 100L, 1000L, LectureProblemSetRole.MAIN);
        given(problemSetCatalogPort.existsProblemSet(1000L)).willReturn(true);
        given(lectureRepository.existsByCourseIdAndLectureIdAndDeletedAtIsNull(10L, 100L)).willReturn(true);

        assertDoesNotThrow(() -> lectureProblemSetPolicy.validate(command));
    }

    @Test
    void validate_allowsFinalProblemSetWithoutLecture() {
        ConfigureLectureProblemSetsCommand command = command(10L, null, 1000L, LectureProblemSetRole.FINAL);
        given(problemSetCatalogPort.existsProblemSet(1000L)).willReturn(true);

        assertDoesNotThrow(() -> lectureProblemSetPolicy.validate(command));
    }

    @Test
    void validate_rejectsMainProblemSetWithoutLecture() {
        ConfigureLectureProblemSetsCommand command = command(10L, null, 1000L, LectureProblemSetRole.MAIN);
        given(problemSetCatalogPort.existsProblemSet(1000L)).willReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProblemSetPolicy.validate(command)
        );

        assertEquals(LectureProblemSetErrorCode.MAIN_LECTURE_REQUIRED, exception.getErrorCode());
    }

    @Test
    void validate_rejectsFinalProblemSetWithLecture() {
        ConfigureLectureProblemSetsCommand command = command(10L, 100L, 1000L, LectureProblemSetRole.FINAL);
        given(problemSetCatalogPort.existsProblemSet(1000L)).willReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProblemSetPolicy.validate(command)
        );

        assertEquals(LectureProblemSetErrorCode.FINAL_LECTURE_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    void validate_rejectsMissingProblemSet() {
        ConfigureLectureProblemSetsCommand command = command(10L, 100L, 1000L, LectureProblemSetRole.MAIN);
        given(problemSetCatalogPort.existsProblemSet(1000L)).willReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProblemSetPolicy.validate(command)
        );

        assertEquals(LectureProblemSetErrorCode.PROBLEM_SET_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void validate_rejectsLectureOutsideCourse() {
        ConfigureLectureProblemSetsCommand command = command(10L, 100L, 1000L, LectureProblemSetRole.MAIN);
        given(problemSetCatalogPort.existsProblemSet(1000L)).willReturn(true);
        given(lectureRepository.existsByCourseIdAndLectureIdAndDeletedAtIsNull(10L, 100L)).willReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProblemSetPolicy.validate(command)
        );

        assertEquals(LectureProblemSetErrorCode.LECTURE_NOT_IN_COURSE, exception.getErrorCode());
    }

    @Test
    void validate_rejectsNullProblemSetItem() {
        ConfigureLectureProblemSetsCommand command = new ConfigureLectureProblemSetsCommand(
                10L,
                Collections.singletonList(null)
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProblemSetPolicy.validate(command)
        );

        assertEquals(LectureProblemSetErrorCode.PROBLEM_SET_REQUIRED, exception.getErrorCode());
    }

    @Test
    void validate_rejectsNonPositiveDisplayOrder() {
        ConfigureLectureProblemSetsCommand command = new ConfigureLectureProblemSetsCommand(
                10L,
                List.of(new ConfigureLectureProblemSetsCommand.ProblemSetCommand(
                        100L,
                        1000L,
                        LectureProblemSetRole.MAIN,
                        0
                ))
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProblemSetPolicy.validate(command)
        );

        assertEquals(LectureProblemSetErrorCode.PROBLEM_STEP_REQUIRED, exception.getErrorCode());
    }

    @Test
    void validate_rejectsDuplicateLectureProblemSetConnection() {
        ConfigureLectureProblemSetsCommand.ProblemSetCommand problemSet =
                new ConfigureLectureProblemSetsCommand.ProblemSetCommand(
                        100L,
                        1000L,
                        LectureProblemSetRole.MAIN,
                        1
                );
        ConfigureLectureProblemSetsCommand command = new ConfigureLectureProblemSetsCommand(
                10L,
                List.of(problemSet, problemSet)
        );
        given(problemSetCatalogPort.existsProblemSet(1000L)).willReturn(true);
        given(lectureRepository.existsByCourseIdAndLectureIdAndDeletedAtIsNull(10L, 100L)).willReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> lectureProblemSetPolicy.validate(command)
        );

        assertEquals(LectureProblemSetErrorCode.DUPLICATE_PROBLEM_SET, exception.getErrorCode());
    }

    private ConfigureLectureProblemSetsCommand command(
            Long courseId,
            Long lectureId,
            Long problemSetId,
            LectureProblemSetRole role
    ) {
        return new ConfigureLectureProblemSetsCommand(
                courseId,
                List.of(new ConfigureLectureProblemSetsCommand.ProblemSetCommand(
                        lectureId,
                        problemSetId,
                        role,
                        1
                ))
        );
    }
}
