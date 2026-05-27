package com.wanted.codebombalms.learning.controller;

import com.wanted.codebombalms.learning.application.command.RecordLectureProgressCommand;
import com.wanted.codebombalms.learning.application.usecase.AdminLearningProgressQueryUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressCommandUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressQueryUseCase;
import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import com.wanted.codebombalms.learning.domain.model.StudentLearningProgress;
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

        mockMvc.perform(get("/api/v1/courses/{courseId}/learning-progress", 101L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LearningResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data[0].userId").value(10L))
                .andExpect(jsonPath("$.data[0].studentName").value("학생"))
                .andExpect(jsonPath("$.data[0].lectureProgressRate").value(50))
                .andExpect(jsonPath("$.data[0].completedProblemCount").value(2L));
    }

    private Authentication authenticatedUser(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId, null);
        authentication.setAuthenticated(true);
        return authentication;
    }
}
