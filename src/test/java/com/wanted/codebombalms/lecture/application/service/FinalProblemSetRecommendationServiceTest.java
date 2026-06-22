package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.lecture.application.port.FinalProblemSetCandidatePort;
import com.wanted.codebombalms.lecture.application.policy.LectureAccessPolicy;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureProblemSetRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FinalProblemSetRecommendationServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LectureProblemSetRepository lectureProblemSetRepository;

    @Mock
    private FinalProblemSetCandidatePort finalProblemSetCandidatePort;

    @Mock
    private LectureAccessPolicy lectureAccessPolicy;

    @InjectMocks
    private FinalProblemSetRecommendationService service;

    @Test
    void findFinalProblemSetCandidates_returnsEmpty_whenLectureHasNoProblemCategory() {
        Lecture lecture = lecture(10L, 1L, null);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));

        var result = service.findFinalProblemSetCandidates(10L, 20L, false);

        assertEquals(0, result.size());
        verify(lectureAccessPolicy).validateLearningContentAccess(lecture, 20L, false);
        verify(finalProblemSetCandidatePort, never()).findCandidates(any(), anySet(), anyInt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findFinalProblemSetCandidates_excludesCourseMainProblemSets() {
        Lecture lecture = lecture(10L, 1L, 3001L);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));
        given(lectureProblemSetRepository.findByCourseIdAndRole(1L, LectureProblemSetRole.MAIN))
                .willReturn(List.of(
                        LectureProblemSet.restore(100L, 1L, 10L, 4001L, LectureProblemSetRole.MAIN, 1),
                        LectureProblemSet.restore(101L, 1L, 11L, 4002L, LectureProblemSetRole.MAIN, 2)
                ));
        given(finalProblemSetCandidatePort.findCandidates(eq(3001L), org.mockito.ArgumentMatchers.anySet(), eq(2)))
                .willReturn(List.of(problemSet(4003L)));

        var result = service.findFinalProblemSetCandidates(10L, 20L, false);

        ArgumentCaptor<Set<Long>> excludedCaptor = ArgumentCaptor.forClass(Set.class);
        verify(lectureAccessPolicy).validateLearningContentAccess(lecture, 20L, false);
        verify(finalProblemSetCandidatePort).findCandidates(eq(3001L), excludedCaptor.capture(), eq(2));
        assertEquals(Set.of(4001L, 4002L), excludedCaptor.getValue());
        assertEquals(1, result.size());
        assertEquals(4003L, result.get(0).problemSetId());
    }

    @Test
    void findFinalProblemSetCandidates_throwsNotFound_whenLectureDoesNotExist() {
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                service.findFinalProblemSetCandidates(10L, 20L, false)
        );

        verify(lectureProblemSetRepository, never()).findByCourseIdAndRole(anyLong(), any());
        verify(finalProblemSetCandidatePort, never()).findCandidates(anyLong(), anySet(), anyInt());
    }

    @Test
    void findFinalProblemSetCandidates_propagatesForbidden_whenAccessDenied() {
        Lecture lecture = lecture(10L, 1L, 3001L);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));
        doThrow(new ForbiddenException(LectureErrorCode.LECTURE_ACCESS_DENIED))
                .when(lectureAccessPolicy).validateLearningContentAccess(lecture, 20L, false);

        assertThrows(ForbiddenException.class, () ->
                service.findFinalProblemSetCandidates(10L, 20L, false)
        );

        verify(lectureProblemSetRepository, never()).findByCourseIdAndRole(anyLong(), any());
        verify(finalProblemSetCandidatePort, never()).findCandidates(anyLong(), anySet(), anyInt());
    }

    private Lecture lecture(Long lectureId, Long courseId, Long problemCategoryId) {
        Course course = new Course();
        course.setCourseId(courseId);

        return Lecture.restore(
                lectureId,
                course,
                "Lecture",
                "description",
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                "lecture.png",
                problemCategoryId,
                LectureStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                1
        );
    }

    private ProblemSetSummary problemSet(Long problemSetId) {
        return ProblemSetSummary.of(
                problemSetId,
                1,
                "Problem Set",
                "description",
                "EASY",
                1,
                2,
                LocalDateTime.now()
        );
    }
}
