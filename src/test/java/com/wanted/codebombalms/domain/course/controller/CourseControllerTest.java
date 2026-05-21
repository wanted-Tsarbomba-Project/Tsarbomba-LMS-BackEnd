package com.wanted.codebombalms.domain.course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.domain.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.domain.course.application.result.CourseDetailResult;
import com.wanted.codebombalms.domain.course.application.result.CourseSummaryResult;
import com.wanted.codebombalms.domain.course.application.usecase.CourseCommandUseCase;
import com.wanted.codebombalms.domain.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;
import com.wanted.codebombalms.domain.course.presentation.api.request.CourseCreateRequest;
import com.wanted.codebombalms.domain.course.presentation.api.request.CourseUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CourseController web test")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseCommandUseCase courseCommandUseCase;

    @MockitoBean
    private CourseQueryUseCase courseQueryUseCase;

    @Test
    void findAllCourses_returnsApiResponse() throws Exception {
        given(courseQueryUseCase.findAllCourses()).willReturn(List.of(
                new CourseSummaryResult(1L, 10L, "Java", "java.png", CourseStatus.ACTIVE)
        ));

        mockMvc.perform(get("/api/v1/courses").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(CourseResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.message").value(CourseResponseMessage.RETRIEVED))
                .andExpect(jsonPath("$.data[0].courseId").value(1L))
                .andExpect(jsonPath("$.data[0].title").value("Java"));
    }

    @Test
    void findCourseById_returnsApiResponse() throws Exception {
        Long courseId = 1L;
        given(courseQueryUseCase.findCourseById(courseId)).willReturn(createDetailResult(courseId, "Java"));

        mockMvc.perform(get("/api/v1/courses/{courseId}", courseId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(CourseResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.message").value(CourseResponseMessage.RETRIEVED))
                .andExpect(jsonPath("$.data.courseId").value(courseId))
                .andExpect(jsonPath("$.data.title").value("Java"));
    }

    @Test
    void createCourse_returnsCreatedApiResponse() throws Exception {
        CourseCreateRequest request = new CourseCreateRequest(10L, "Java", "description", "java.png");
        given(courseCommandUseCase.createCourse(any(CreateCourseCommand.class)))
                .willReturn(createDetailResult(1L, "Java"));

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value(CourseResponseCode.CREATED))
                .andExpect(jsonPath("$.message").value(CourseResponseMessage.CREATED))
                .andExpect(jsonPath("$.data.courseId").value(1L));
    }

    @Test
    void updateCourse_returnsApiResponse() throws Exception {
        Long courseId = 1L;
        CourseUpdateRequest request = new CourseUpdateRequest("Updated Java", "updated", "updated.png", CourseStatus.ACTIVE);
        given(courseCommandUseCase.updateCourse(any(UpdateCourseCommand.class)))
                .willReturn(createDetailResult(courseId, "Updated Java"));

        mockMvc.perform(put("/api/v1/courses/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(CourseResponseCode.UPDATED))
                .andExpect(jsonPath("$.message").value(CourseResponseMessage.UPDATED))
                .andExpect(jsonPath("$.data.title").value("Updated Java"));
    }

    @Test
    void publishCourse_returnsApiResponse() throws Exception {
        Long courseId = 1L;
        given(courseCommandUseCase.publishCourse(any(PublishCourseCommand.class)))
                .willReturn(createDetailResult(courseId, "Java"));

        mockMvc.perform(patch("/api/v1/courses/{courseId}/publish", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(CourseResponseCode.PUBLISHED))
                .andExpect(jsonPath("$.message").value(CourseResponseMessage.PUBLISHED));
    }

    @Test
    void deleteCourse_returnsNoContent() throws Exception {
        Long courseId = 1L;

        mockMvc.perform(delete("/api/v1/courses/{courseId}", courseId))
                .andExpect(status().isNoContent());

        verify(courseCommandUseCase).deleteCourse(courseId);
    }

    private CourseDetailResult createDetailResult(Long courseId, String title) {
        return new CourseDetailResult(
                courseId,
                10L,
                title,
                "description",
                "java.png",
                CourseStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
