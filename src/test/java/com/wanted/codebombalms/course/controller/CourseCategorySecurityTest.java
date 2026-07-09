package com.wanted.codebombalms.course.controller;

import com.wanted.codebombalms.admin.permission.application.service.AdminPermissionCheckService;
import com.wanted.codebombalms.course.application.usecase.CourseCategoryQueryUseCase;
import com.wanted.codebombalms.course.domain.model.CourseCategory;
import com.wanted.codebombalms.course.domain.model.CourseCategoryStatus;
import com.wanted.codebombalms.course.presentation.api.CourseCategoryController;
import com.wanted.codebombalms.global.infrastructure.config.security.CustomAccessDeniedHandler;
import com.wanted.codebombalms.global.infrastructure.config.security.CustomAuthenticationEntryPoint;
import com.wanted.codebombalms.global.infrastructure.config.security.SecurityConfig;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseCategoryController.class)
@Import({
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class
})
@DisplayName("CourseCategory security test")
class CourseCategorySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseCategoryQueryUseCase courseCategoryQueryUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @MockitoBean
    private AdminPermissionCheckService adminPermissionCheckService;

    @Test
    void findCourseCategories_isPublicBeforeLogin() throws Exception {
        given(courseCategoryQueryUseCase.findCourseCategories()).willReturn(List.of(
                CourseCategory.restore(
                        1L,
                        "Python",
                        CourseCategoryStatus.ACTIVE,
                        1,
                        LocalDateTime.now()
                )
        ));

        mockMvc.perform(get("/api/v1/course-categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].courseCategoryId").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Python"));
    }

    @Test
    void writeMethodToCourseCategories_requiresAuthenticationBeforeLogin() throws Exception {
        mockMvc.perform(post("/api/v1/course-categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
