package com.wanted.codebombalms.lecture.controller;

import com.wanted.codebombalms.admin.permission.application.service.AdminPermissionCheckService;
import com.wanted.codebombalms.global.presentation.api.common.GlobalExceptionHandler;
import com.wanted.codebombalms.lecture.application.command.UpdateLectureOrdersCommand;
import com.wanted.codebombalms.lecture.application.usecase.FinalProblemSetRecommendationUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureMaterialUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.codebombalms.lecture.presentation.api.LectureController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LectureController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
        LectureController.class,
        GlobalExceptionHandler.class,
        LectureControllerSecurityTest.TestSecurityConfig.class
})
class LectureControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LectureCommandUseCase lectureCommandUseCase;

    @MockitoBean
    private LectureQueryUseCase lectureQueryUseCase;

    @MockitoBean
    private FinalProblemSetRecommendationUseCase finalProblemSetRecommendationUseCase;

    @MockitoBean
    private LectureMaterialUseCase lectureMaterialUseCase;

    @MockitoBean
    private AdminPermissionCheckService adminPermissionCheckService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Test
    void updateLectureOrders_rejectsStudent() throws Exception {
        String request = """
                {
                  "lectures": [
                    { "lectureId": 1, "lectureOrder": 1 }
                  ]
                }
                """;

        mockMvc.perform(put("/api/v1/courses/{courseId}/lectures/order", 1L)
                        .with(authentication(studentUser(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

        verify(lectureCommandUseCase, never()).updateLectureOrders(any(UpdateLectureOrdersCommand.class));
    }

    private Authentication studentUser(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId, null, "ROLE_STUDENT");
        authentication.setAuthenticated(true);
        return authentication;
    }
}
