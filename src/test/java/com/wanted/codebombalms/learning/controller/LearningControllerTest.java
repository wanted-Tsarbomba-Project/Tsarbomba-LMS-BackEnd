package com.wanted.codebombalms.learning.controller;

import com.wanted.codebombalms.learning.application.command.RecordLectureProgressCommand;
import com.wanted.codebombalms.learning.application.command.RecordLectureProblemProgressCommand;
import com.wanted.codebombalms.learning.application.usecase.AdminLearningProgressQueryUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemProgressCommandUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemSetQueryUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemSubmissionUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressCommandUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressQueryUseCase;
import com.wanted.codebombalms.learning.domain.model.CourseLearningProgress;
import com.wanted.codebombalms.learning.domain.model.LearningProgressSummary;
import com.wanted.codebombalms.learning.domain.model.LectureLearningProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProblemStatistics;
import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import com.wanted.codebombalms.learning.domain.model.StudentLearningProgress;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LearningController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("LearningController web test")
class LearningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LectureProgressCommandUseCase lectureProgressCommandUseCase;

    @MockitoBean
    private LectureProgressQueryUseCase lectureProgressQueryUseCase;

    @MockitoBean
    private LectureProblemProgressCommandUseCase lectureProblemProgressCommandUseCase;

    @MockitoBean
    private LectureProblemSetQueryUseCase lectureProblemSetQueryUseCase;

    @MockitoBean
    private LectureProblemSubmissionUseCase lectureProblemSubmissionUseCase;

    @MockitoBean
    private AdminLearningProgressQueryUseCase adminLearningProgressQueryUseCase;

    @Test
    void recordLectureProgressReturnsApiResponse() throws Exception {
        given(lectureProgressCommandUseCase.recordProgress(any(RecordLectureProgressCommand.class)))
                .willReturn(LectureProgress.restore(
                        1L,
                        10L,
                        101L,
                        true,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        null
                ));

        mockMvc.perform(patch("/api/v1/lectures/{lectureId}/progress", 101L)
                        .with(authentication(authenticatedUser(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "completed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.UPDATED))
                .andExpect(jsonPath("$.data.lectureProgressId").value(1L))
                .andExpect(jsonPath("$.data.lectureId").value(101L))
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    void findLectureProgressReturnsApiResponse() throws Exception {
        given(lectureProgressQueryUseCase.findProgress(nullable(Long.class), nullable(Long.class)))
                .willReturn(LectureProgress.restore(
                        1L,
                        10L,
                        101L,
                        false,
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        null
                ));

        mockMvc.perform(get("/api/v1/lectures/{lectureId}/progress", 101L)
                        .with(authentication(authenticatedUser(10L)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data.lectureProgressId").value(1L))
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    void enterLectureProblemSetReturnsApiResponse() throws Exception {
        given(lectureProblemSetQueryUseCase.enterLectureProblemSet(10L, 6001L))
                .willReturn(new LectureProblemSetQueryUseCase.LectureProblemSetEntryView(
                        6001L,
                        2001L,
                        "Problem Set",
                        "description",
                        1,
                        false,
                        new LectureProblemSetQueryUseCase.ProblemDetailView(
                                3001L,
                                1,
                                "Problem 1",
                                "content",
                                "CODE",
                                10,
                                "print()"
                        )
                ));

        mockMvc.perform(get("/api/v1/lecture-problem-sets/{lectureProblemSetId}", 6001L)
                        .param("userId", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data.lectureProblemSetId").value(6001L))
                .andExpect(jsonPath("$.data.problemSetId").value(2001L))
                .andExpect(jsonPath("$.data.problem.problemId").value(3001L));
    }

    @Test
    void findLectureProblemSetProgressReturnsApiResponse() throws Exception {
        given(lectureProblemSetQueryUseCase.findLectureProblemSetProgress(10L, 6001L))
                .willReturn(new LectureProblemSetQueryUseCase.LectureProblemSetProgressView(
                        6001L,
                        2001L,
                        3,
                        2,
                        3002L,
                        1,
                        false,
                        List.of(new LectureProblemSetQueryUseCase.ProblemProgressItemView(3001L, 1, "CORRECT"))
                ));

        mockMvc.perform(get("/api/v1/lecture-problem-sets/{lectureProblemSetId}/progress", 6001L)
                        .param("userId", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data.currentProblemNumber").value(2))
                .andExpect(jsonPath("$.data.problems[0].status").value("CORRECT"));
    }

    @Test
    void recordLectureProblemSetProgressReturnsApiResponse() throws Exception {
        given(lectureProblemProgressCommandUseCase.recordProgress(any(RecordLectureProblemProgressCommand.class)))
                .willReturn(LectureProblemProgress.restore(
                        1L,
                        10L,
                        6001L,
                        2,
                        false,
                        null,
                        LocalDateTime.now(),
                        null
                ));

        mockMvc.perform(patch("/api/v1/lecture-problem-sets/{lectureProblemSetId}/progress", 6001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 10,
                                  "currentProblemNumber": 2,
                                  "completed": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.UPDATED))
                .andExpect(jsonPath("$.data.lectureProblemSetId").value(6001L))
                .andExpect(jsonPath("$.data.currentProblemNumber").value(2));
    }

    @Test
    void submitLectureProblemReturnsApiResponse() throws Exception {
        given(lectureProblemSubmissionUseCase.submit(
                any(Long.class),
                any(Long.class),
                any(SubmitCodeCommand.class)
        )).willReturn(new SubmissionCommandUseCase.SubmissionView(
                1L,
                3001L,
                true,
                2,
                2,
                "SUCCESS",
                null,
                1,
                0,
                false,
                3002L,
                false,
                10,
                true,
                "explanation"
        ));

        mockMvc.perform(post("/api/v1/lecture-problem-sets/{lectureProblemSetId}/problems/{problemId}/submissions", 6001L, 3001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 10,
                                  "code": "print('hello')"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.SUBMITTED))
                .andExpect(jsonPath("$.data.submissionId").value(1L))
                .andExpect(jsonPath("$.data.problemId").value(3001L))
                .andExpect(jsonPath("$.data.isCorrect").value(true));
    }

    @Test
    void findStudentLearningProgressesReturnsApiResponse() throws Exception {
        given(adminLearningProgressQueryUseCase.findStudentProgresses(101L))
                .willReturn(List.of(StudentLearningProgress.of(
                        10L,
                        "학생",
                        1L,
                        2L,
                        2L,
                        3L
                )));

        mockMvc.perform(get("/api/v1/courses/{courseId}/users/learning-progress", 101L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data[0].userId").value(10L))
                .andExpect(jsonPath("$.data[0].studentName").value("학생"))
                .andExpect(jsonPath("$.data[0].lectureProgressRate").value(50))
                .andExpect(jsonPath("$.data[0].completedProblemCount").value(2L));
    }

    @Test
    void findCourseLearningProgressReturnsApiResponse() throws Exception {
        given(adminLearningProgressQueryUseCase.findCourseProgress(101L))
                .willReturn(CourseLearningProgress.of(101L, "Java", 2L, 3L, 4L, 5L, 6L));

        mockMvc.perform(get("/api/v1/courses/{courseId}/learning-progress", 101L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data.courseId").value(101L))
                .andExpect(jsonPath("$.data.averageLectureProgressRate").value(75));
    }

    @Test
    void findCourseLearningProgressesReturnsApiResponse() throws Exception {
        given(adminLearningProgressQueryUseCase.findCourseProgresses())
                .willReturn(List.of(CourseLearningProgress.of(101L, "Java", 2L, 3L, 4L, 5L, 6L)));

        mockMvc.perform(get("/api/v1/courses/learning-progress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data[0].courseId").value(101L));
    }

    @Test
    void findStudentLearningProgressReturnsApiResponse() throws Exception {
        given(adminLearningProgressQueryUseCase.findStudentProgress(101L, 10L))
                .willReturn(StudentLearningProgress.of(10L, "?숈깮", 1L, 2L, 2L, 3L));

        mockMvc.perform(get("/api/v1/courses/{courseId}/users/{userId}/learning-progress", 101L, 10L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data.userId").value(10L));
    }

    @Test
    void findLectureLearningProgressesReturnsApiResponse() throws Exception {
        given(adminLearningProgressQueryUseCase.findLectureProgresses(101L))
                .willReturn(List.of(LectureLearningProgress.of(201L, "OT", 1L, 2L)));

        mockMvc.perform(get("/api/v1/courses/{courseId}/lectures/learning-progress", 101L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data[0].lectureId").value(201L))
                .andExpect(jsonPath("$.data[0].progressRate").value(50));
    }

    @Test
    void findLectureProblemStatisticsReturnsApiResponse() throws Exception {
        given(adminLearningProgressQueryUseCase.findLectureProblemStatistics(201L))
                .willReturn(LectureProblemStatistics.of(201L, 2L, 4L));

        mockMvc.perform(get("/api/v1/lectures/{lectureId}/problems/statistics", 201L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data.lectureId").value(201L))
                .andExpect(jsonPath("$.data.completionRate").value(50));
    }

    @Test
    void summarizeLearningProgressReturnsApiResponse() throws Exception {
        given(adminLearningProgressQueryUseCase.summarizeLearningProgress())
                .willReturn(LearningProgressSummary.of(2L, 3L, 4L, 5L, 6L, 7L));

        mockMvc.perform(get("/api/v1/learning-progress/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data.totalCourseCount").value(2L))
                .andExpect(jsonPath("$.data.averageLectureProgressRate").value(80));
    }

    private Authentication authenticatedUser(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId, null);
        authentication.setAuthenticated(true);
        return authentication;
    }
}
