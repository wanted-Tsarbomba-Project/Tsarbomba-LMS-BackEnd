package com.wanted.codebombalms.domain.enrollment.presentation.api;

import com.wanted.codebombalms.enrollment.application.service.EnrollmentService;
import com.wanted.codebombalms.enrollment.controller.EnrollmentController;
import com.wanted.codebombalms.enrollment.controller.EnrollmentResponseCode;
import com.wanted.codebombalms.enrollment.controller.EnrollmentResponseMessage;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.presentation.api.request.EnrollmentCreateRequest;
import com.wanted.codebombalms.enrollment.presentation.api.response.EnrollmentResponse;
import com.wanted.codebombalms.enrollment.presentation.api.response.MyCourseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("EnrollmentController web test")
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollmentService enrollmentService;

    @Test
    void createEnrollment_returnsApiResponse() throws Exception {
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

        mockMvc.perform(post("/api/v1/courses/{courseId}/enrollments", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value(EnrollmentResponseCode.CREATED))
                .andExpect(jsonPath("$.message").value(EnrollmentResponseMessage.CREATED))
                .andExpect(jsonPath("$.data.enrollmentId").value(1L))
                .andExpect(jsonPath("$.data.studentId").value(studentId));
    }

    @Test
    void findMyCourses_returnsApiResponse() throws Exception {
        Long studentId = 10L;
        given(enrollmentService.findMyCourses(studentId)).willReturn(List.of(
                new MyCourseResponse(1L, 1L, "Java", "description", "java.png", EnrollmentStatus.ACTIVE, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/v1/students/{studentId}/enrollments", studentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(EnrollmentResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.message").value(EnrollmentResponseMessage.RETRIEVED))
                .andExpect(jsonPath("$.data[0].courseTitle").value("Java"));
    }

    @Test
    void cancelEnrollment_returnsNoContent() throws Exception {
        Long studentId = 10L;
        Long enrollmentId = 1L;

        mockMvc.perform(delete("/api/v1/students/{studentId}/enrollments/{enrollmentId}", studentId, enrollmentId))
                .andExpect(status().isNoContent());

        verify(enrollmentService).cancelEnrollment(studentId, enrollmentId);
    }
}
