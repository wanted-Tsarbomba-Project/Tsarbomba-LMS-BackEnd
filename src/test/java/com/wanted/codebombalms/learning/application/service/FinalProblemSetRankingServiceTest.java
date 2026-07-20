package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningCoursePort;
import com.wanted.codebombalms.learning.application.port.LearningLecture;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSet;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSetPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemExplanationPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningRecommendationRequest;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningRecommendationResult;
import com.wanted.codebombalms.learning.domain.model.LearningCourse;
import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemSubmissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FinalProblemSetRankingServiceTest {

    @Mock
    private LearningRecommendationClient learningRecommendationClient;
    @Mock
    private LearningCoursePort learningCoursePort;
    @Mock
    private LearningLecturePort learningLecturePort;
    @Mock
    private LearningCourseProblemPort learningCourseProblemPort;
    @Mock
    private LearningLectureProblemSetPort learningLectureProblemSetPort;
    @Mock
    private LearningProblemPort learningProblemPort;
    @Mock
    private LearningProblemExplanationPort learningProblemExplanationPort;
    @Mock
    private LectureProblemSubmissionRepository lectureProblemSubmissionRepository;

    @InjectMocks
    private FinalProblemSetRankingService service;

    @Test
    void rankFinalProblemSets_buildsAllMainLearningMetrics() {
        given(learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(1L))
                .willReturn(List.of(100L, 101L));
        given(learningLectureProblemSetPort.findLectureProblemSet(100L))
                .willReturn(new LearningLectureProblemSet(100L, 1L, 10L, 1000L));
        given(learningLectureProblemSetPort.findLectureProblemSet(101L))
                .willReturn(new LearningLectureProblemSet(101L, 1L, 11L, 1001L));
        given(learningProblemPort.loadProblemSet(1000L))
                .willReturn(problemSet(1000L, problem(1L, 1), problem(2L, 2)));
        given(learningProblemPort.loadProblemSet(1001L))
                .willReturn(problemSet(1001L, problem(3L, 1)));
        given(lectureProblemSubmissionRepository.findByUserIdAndLectureProblemSetId(20L, 100L))
                .willReturn(List.of(
                        submission(1L, 100L, 1L, true, 1, 2, 2, LocalDateTime.of(2026, 7, 20, 10, 0)),
                        submission(2L, 100L, 2L, false, 1, 0, 2, LocalDateTime.of(2026, 7, 20, 10, 1)),
                        submission(3L, 100L, 2L, true, 2, 1, 2, LocalDateTime.of(2026, 7, 20, 10, 2))
                ));
        given(lectureProblemSubmissionRepository.findByUserIdAndLectureProblemSetId(20L, 101L))
                .willReturn(List.of());
        given(learningProblemExplanationPort.findViewedProblemIds(20L, List.of(1L, 2L, 3L)))
                .willReturn(Set.of(2L, 3L));
        given(learningCoursePort.findActiveCourse(1L))
                .willReturn(new LearningCourse(1L, "Python", "Python basics"));
        given(learningLecturePort.findLecturesByCourse(1L))
                .willReturn(List.of(new LearningLecture(10L, "Loop", "for and while")));
        given(learningRecommendationClient.rankFinalProblemSets(org.mockito.ArgumentMatchers.any()))
                .willReturn(new LearningRecommendationResult("algorithm", List.of()));

        service.rankFinalProblemSets(20L, 1L, 10L, 3001L, Set.of(1000L), false);

        ArgumentCaptor<LearningRecommendationRequest> captor =
                ArgumentCaptor.forClass(LearningRecommendationRequest.class);
        verify(learningRecommendationClient).rankFinalProblemSets(captor.capture());
        var request = captor.getValue();
        var profile = request.learningProfile();

        assertEquals(3, profile.totalMainProblemCount());
        assertEquals(2, profile.correctProblemCount());
        assertEquals(33.3333, profile.directSolveRate());
        assertEquals(2, profile.explanationViewCount());
        assertEquals(66.6667, profile.explanationViewRate());
        assertEquals(1.0, profile.averageAttemptCount());
        assertEquals(50.0, profile.averageTestPassRate());
        assertEquals(List.of(1000L), request.excludedProblemSetIds());
        assertEquals("Python basics", request.learningContext().courseDescription());
        assertEquals("for and while", request.learningContext().lectures().get(0).description());
    }

    @Test
    void rankFinalProblemSets_sendsZeroProfile_whenCourseHasNoMainProblems() {
        given(learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(1L))
                .willReturn(List.of());
        given(learningCoursePort.findActiveCourse(1L))
                .willReturn(new LearningCourse(1L, "Python", null));
        given(learningLecturePort.findLecturesByCourse(1L)).willReturn(List.of());
        given(learningRecommendationClient.rankFinalProblemSets(org.mockito.ArgumentMatchers.any()))
                .willReturn(new LearningRecommendationResult("algorithm", List.of()));

        service.rankFinalProblemSets(20L, 1L, 10L, 3001L, Set.of(), false);

        ArgumentCaptor<LearningRecommendationRequest> captor =
                ArgumentCaptor.forClass(LearningRecommendationRequest.class);
        verify(learningRecommendationClient).rankFinalProblemSets(captor.capture());
        assertEquals(LearningRecommendationClient.LearningProfile.empty(),
                captor.getValue().learningProfile());
        verify(learningProblemExplanationPort, never())
                .findViewedProblemIds(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void rankFinalProblemSets_sendsZeroProfile_forOperatorPreview() {
        given(learningCoursePort.findActiveCourse(1L))
                .willReturn(new LearningCourse(1L, "Python", null));
        given(learningLecturePort.findLecturesByCourse(1L)).willReturn(List.of());
        given(learningRecommendationClient.rankFinalProblemSets(org.mockito.ArgumentMatchers.any()))
                .willReturn(new LearningRecommendationResult("algorithm", List.of()));

        service.rankFinalProblemSets(null, 1L, 10L, 3001L, Set.of(), true);

        ArgumentCaptor<LearningRecommendationRequest> captor =
                ArgumentCaptor.forClass(LearningRecommendationRequest.class);
        verify(learningRecommendationClient).rankFinalProblemSets(captor.capture());
        assertEquals(LearningRecommendationClient.LearningProfile.empty(),
                captor.getValue().learningProfile());
        verify(learningCourseProblemPort, never()).findMainLectureProblemSetIdsByCourse(1L);
    }

    private LearningProblemPort.ProblemSetForLearning problemSet(
            Long problemSetId,
            LearningProblemPort.ProblemDetailForLearning... problems
    ) {
        return new LearningProblemPort.ProblemSetForLearning(
                problemSetId,
                "Problem Set",
                "description",
                List.of(problems)
        );
    }

    private LearningProblemPort.ProblemDetailForLearning problem(Long problemId, int number) {
        return new LearningProblemPort.ProblemDetailForLearning(
                problemId,
                number,
                "Problem",
                "content",
                "SQL",
                0,
                ""
        );
    }

    private LectureProblemSubmission submission(
            Long submissionId,
            Long lectureProblemSetId,
            Long problemId,
            boolean correct,
            int attemptNo,
            int passedTestCount,
            int totalTestCount,
            LocalDateTime submittedAt
    ) {
        return new LectureProblemSubmission(
                submissionId,
                20L,
                lectureProblemSetId,
                problemId,
                "code",
                correct,
                attemptNo,
                passedTestCount,
                totalTestCount,
                "SUCCESS",
                null,
                submittedAt
        );
    }
}
