package com.wanted.codebombalms.domain.enrollment.presentation.api;

import com.wanted.codebombalms.domain.enrollment.presentation.api.request.EnrollmentCreateRequest;
import com.wanted.codebombalms.domain.enrollment.presentation.api.response.EnrollmentResponse;
import com.wanted.codebombalms.domain.enrollment.presentation.api.response.MyCourseResponse;
import com.wanted.codebombalms.domain.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.domain.enrollment.application.service.EnrollmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("EnrollmentController 웹 계층 테스트")
public class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollmentService enrollmentService;

    @Test
    @DisplayName("수강 신청 API가 정상 응답을 반환한다.")
    void 수강_신청_테스트() throws Exception {

        // given
        Long courseId = 1L;
        Long studentId = 10L;

        EnrollmentResponse response = new EnrollmentResponse(
                1L,
                courseId,
                studentId,
                EnrollmentStatus.ACTIVE,
                LocalDateTime.now(),
                null
        );

        given(enrollmentService.createEnrollment(eq(courseId), any(EnrollmentCreateRequest.class)))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/courses/{courseId}/enrollments", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 10
                                }
                                """)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("수강 신청 성공"))
                .andExpect(jsonPath("$.data.enrollmentId").value(1L))
                .andExpect(jsonPath("$.data.courseId").value(courseId))
                .andExpect(jsonPath("$.data.studentId").value(studentId))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("내 수강 강좌 목록 조회 API가 정상 응답을 반환한다.")
    void 내_수강_강좌_목록_조회_테스트() throws Exception {

        // given
        Long studentId = 10L;

        List<MyCourseResponse> response = List.of(
                new MyCourseResponse(
                        1L,
                        1L,
                        "Java 기초 강좌",
                        "Java 기초 문법을 학습하는 강좌입니다.",
                        "java.png",
                        EnrollmentStatus.ACTIVE,
                        LocalDateTime.now()
                ),
                new MyCourseResponse(
                        2L,
                        2L,
                        "Spring 기초 강좌",
                        "Spring 기초를 학습하는 강좌입니다.",
                        "spring.png",
                        EnrollmentStatus.ACTIVE,
                        LocalDateTime.now()
                )
        );

        given(enrollmentService.findMyCourses(studentId)).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/students/{studentId}/enrollments", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("내 수강 강좌 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].enrollmentId").value(1L))
                .andExpect(jsonPath("$.data[0].courseId").value(1L))
                .andExpect(jsonPath("$.data[0].courseTitle").value("Java 기초 강좌"))
                .andExpect(jsonPath("$.data[1].enrollmentId").value(2L))
                .andExpect(jsonPath("$.data[1].courseId").value(2L))
                .andExpect(jsonPath("$.data[1].courseTitle").value("Spring 기초 강좌"));
    }

    @Test
    @DisplayName("수강 신청 취소 API가 204 상태 코드를 반환한다.")
    void 수강_신청_취소_테스트() throws Exception {

        // given
        Long studentId = 10L;
        Long enrollmentId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/students/{studentId}/enrollments/{enrollmentId}",
                        studentId,
                        enrollmentId)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(enrollmentService).cancelEnrollment(studentId, enrollmentId);
    }
}