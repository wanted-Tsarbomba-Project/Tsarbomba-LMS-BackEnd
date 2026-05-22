package com.wanted.codebombalms.domain.enrollment.presentation.api;

import com.wanted.codebombalms.enrollment.application.command.CancelEnrollmentCommand;
import com.wanted.codebombalms.enrollment.application.command.EnrollCourseCommand;
import com.wanted.codebombalms.enrollment.application.port.CourseCatalogPort;
import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.codebombalms.enrollment.controller.EnrollmentController;
import com.wanted.codebombalms.enrollment.controller.EnrollmentResponseCode;
import com.wanted.codebombalms.enrollment.controller.EnrollmentResponseMessage;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
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
    private EnrollmentCommandUseCase enrollmentCommandUseCase;

    @MockitoBean
    private EnrollmentQueryUseCase enrollmentQueryUseCase;

    @MockitoBean
    private CourseCatalogPort courseCatalogPort;

    @Test
    void createEnrollment_returnsApiResponse() throws Exception {
        Long courseId = 1L;
        Long studentId = 10L;
        Enrollment enrollment = createEnrollment(1L, studentId, courseId, EnrollmentStatus.ACTIVE);

        given(enrollmentCommandUseCase.createEnrollment(any(EnrollCourseCommand.class)))
                .willReturn(enrollment);

        mockMvc.perform(post("/api/v1/courses/{courseId}/enrollments", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":10,\"courseId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value(EnrollmentResponseCode.CREATED))
                .andExpect(jsonPath("$.message").value(EnrollmentResponseMessage.CREATED))
                .andExpect(jsonPath("$.data.enrollmentId").value(1L));
    }

    @Test
    void findMyCourses_returnsApiResponse() throws Exception {
        Long studentId = 10L;
        Enrollment enrollment = createEnrollment(1L, studentId, 1L, EnrollmentStatus.ACTIVE);

        given(enrollmentQueryUseCase.findMyCourses(studentId)).willReturn(List.of(enrollment));
        given(courseCatalogPort.getPublicationStatus(1L))
                .willReturn(new CoursePublicationStatus(1L, 1L, "Java", "description", "java.png", true));

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

        verify(enrollmentCommandUseCase).cancelEnrollment(eq(new CancelEnrollmentCommand(studentId, enrollmentId)));
    }

    private Enrollment createEnrollment(
            Long enrollmentId,
            Long userId,
            Long courseId,
            EnrollmentStatus status
    ) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(enrollmentId);
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setStatus(status);
        enrollment.setEnrolledAt(LocalDateTime.now());
        return enrollment;
    }
}
