package com.wanted.codebombalms.enrollment.presentation.api;

import com.wanted.codebombalms.enrollment.application.command.CancelEnrollmentCommand;
import com.wanted.codebombalms.enrollment.application.command.EnrollCourseCommand;
import com.wanted.codebombalms.enrollment.application.port.CourseCatalogPort;
import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.application.query.MyCourseResult;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
        EnrollmentController.class,
        EnrollmentControllerTest.TestSecurityConfig.class
})
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

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }

    @Test
    void createEnrollment_returnsApiResponse() throws Exception {
        Long courseId = 1L;
        Long studentId = 10L;
        Enrollment enrollment = createEnrollment(1L, studentId, courseId, EnrollmentStatus.ACTIVE);

        given(enrollmentCommandUseCase.createEnrollment(any(EnrollCourseCommand.class)))
                .willReturn(enrollment);

        mockMvc.perform(post("/api/v1/courses/{courseId}/enrollments", courseId)
                        .with(authentication(studentPrincipal(studentId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value(EnrollmentResponseCode.CREATED))
                .andExpect(jsonPath("$.message").value(EnrollmentResponseMessage.CREATED))
                .andExpect(jsonPath("$.data.enrollmentId").value(1L));
    }

    private UsernamePasswordAuthenticationToken studentPrincipal(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    private UsernamePasswordAuthenticationToken adminPrincipal(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    void findMyCourses_returnsApiResponse() throws Exception {
        Long studentId = 10L;
        Enrollment enrollment = createEnrollment(1L, studentId, 1L, EnrollmentStatus.ACTIVE);

        given(enrollmentQueryUseCase.findMyCourses(studentId)).willReturn(List.of(createMyCourseResult(enrollment)));

        mockMvc.perform(get("/api/v1/users/{userId}/enrollments", studentId)
                        .with(authentication(adminPrincipal(1L)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(EnrollmentResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.message").value(EnrollmentResponseMessage.RETRIEVED))
                .andExpect(jsonPath("$.data[0].courseTitle").value("Java"));
    }

    @Test
    void findMyCoursesByMe_returnsApiResponse() throws Exception {
        Long studentId = 10L;
        Enrollment enrollment = createEnrollment(1L, studentId, 1L, EnrollmentStatus.ACTIVE);

        given(enrollmentQueryUseCase.findMyCourses(studentId)).willReturn(List.of(createMyCourseResult(enrollment)));

        mockMvc.perform(get("/api/v1/users/me/enrollments")
                        .with(authentication(studentPrincipal(studentId)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(EnrollmentResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.message").value(EnrollmentResponseMessage.RETRIEVED))
                .andExpect(jsonPath("$.data[0].studentId").value(studentId))
                .andExpect(jsonPath("$.data[0].courseTitle").value("Java"));
    }

    @Test
    void findAllEnrollments_returnsApiResponse() throws Exception {
        Enrollment enrollment = createEnrollment(1L, 10L, 1L, EnrollmentStatus.ACTIVE);

        given(enrollmentQueryUseCase.findAllActiveEnrollments()).willReturn(List.of(enrollment));
        given(courseCatalogPort.getPublicationStatus(1L))
                .willReturn(new CoursePublicationStatus(1L, 1L, "Java", "description", "java.png", true));

        mockMvc.perform(get("/api/v1/enrollments")
                        .with(authentication(operatorPrincipal(1L)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(EnrollmentResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data[0].courseTitle").value("Java"));
    }

    @Test
    void createEnrollment_forbiddenForOperator() throws Exception {
        mockMvc.perform(post("/api/v1/courses/{courseId}/enrollments", 1L)
                        .with(authentication(operatorPrincipal(20L)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void findMyCoursesByMe_forbiddenForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/enrollments")
                        .with(authentication(adminPrincipal(1L)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelMyEnrollment_forbiddenForOperator() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me/enrollments/{enrollmentId}", 1L)
                        .with(authentication(operatorPrincipal(20L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelEnrollment_returnsNoContent() throws Exception {
        Long studentId = 10L;
        Long enrollmentId = 1L;

        mockMvc.perform(delete("/api/v1/users/{userId}/enrollments/{enrollmentId}", studentId, enrollmentId)
                        .with(authentication(studentPrincipal(studentId))))
                .andExpect(status().isNoContent());

        verify(enrollmentCommandUseCase).cancelEnrollment(eq(new CancelEnrollmentCommand(studentId, enrollmentId)));
    }

    @Test
    void cancelEnrollment_forbiddenWhenPathUserDiffersFromPrincipal() throws Exception {
        Long studentId = 10L;
        Long otherUserId = 11L;
        Long enrollmentId = 1L;

        mockMvc.perform(delete("/api/v1/users/{userId}/enrollments/{enrollmentId}", otherUserId, enrollmentId)
                        .with(authentication(studentPrincipal(studentId))))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelMyEnrollment_returnsNoContent() throws Exception {
        Long studentId = 10L;
        Long enrollmentId = 1L;

        mockMvc.perform(delete("/api/v1/users/me/enrollments/{enrollmentId}", enrollmentId)
                        .with(authentication(studentPrincipal(studentId))))
                .andExpect(status().isNoContent());

        verify(enrollmentCommandUseCase).cancelEnrollment(eq(new CancelEnrollmentCommand(studentId, enrollmentId)));
    }

    private UsernamePasswordAuthenticationToken operatorPrincipal(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))
        );
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

    private MyCourseResult createMyCourseResult(Enrollment enrollment) {
        return new MyCourseResult(
                enrollment.getEnrollmentId(),
                enrollment.getUserId(),
                enrollment.getCourseId(),
                1L,
                "Java",
                "description",
                "java.png",
                enrollment.getStatus(),
                enrollment.getEnrolledAt()
        );
    }
}
