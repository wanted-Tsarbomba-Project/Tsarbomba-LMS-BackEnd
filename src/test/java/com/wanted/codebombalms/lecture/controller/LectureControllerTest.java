package com.wanted.codebombalms.lecture.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.admin.permission.application.service.AdminPermissionCheckService;
import com.wanted.codebombalms.lecture.application.command.CreateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UpdateLectureCommand;
import com.wanted.codebombalms.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.lecture.presentation.api.LectureController;
import com.wanted.codebombalms.lecture.presentation.api.LectureResponseCode;
import com.wanted.codebombalms.lecture.presentation.api.LectureResponseMessage;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.lecture.presentation.api.request.LectureUpdateRequest;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LectureController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("LectureController web test")
class LectureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LectureCommandUseCase lectureCommandUseCase;

    @MockitoBean
    private LectureQueryUseCase lectureQueryUseCase;

    @MockitoBean
    private AdminPermissionCheckService adminPermissionCheckService;

    @Test
    void findLecturesByCourseId_returnsApiResponse() throws Exception {
        Long courseId = 1L;
        given(lectureQueryUseCase.findLecturesByCourseId(courseId)).willReturn(List.of(
                createLecture(1L, createCourse(courseId), "Java 1")
        ));

        mockMvc.perform(get("/api/v1/courses/{courseId}/lectures", courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(LectureResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.message").value(LectureResponseMessage.RETRIEVED))
                .andExpect(jsonPath("$.data[0].title").value("Java 1"));
    }

    @Test
    void findLectureById_returnsApiResponse() throws Exception {
        Long lectureId = 1L;
        given(lectureQueryUseCase.findLectureById(lectureId)).willReturn(createDetailResult(lectureId, "Java 1"));

        mockMvc.perform(get("/api/v1/lectures/{lectureId}", lectureId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(LectureResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.message").value(LectureResponseMessage.RETRIEVED))
                .andExpect(jsonPath("$.data.lectureId").value(lectureId));
    }

    @Test
    void createLecture_returnsApiResponse() throws Exception {
        Long courseId = 1L;
        LectureCreateRequest request = new LectureCreateRequest(
                "Java 1",
                "description",
                "java-1.mp4",
                "java-1.png",
                1,
                LectureStatus.ACTIVE
        );
        given(lectureCommandUseCase.createLecture(any(CreateLectureCommand.class)))
                .willReturn(createDetailResult(1L, "Java 1"));

        mockMvc.perform(post("/api/v1/courses/{courseId}/lectures", courseId)
                        .with(authentication(operatorUser(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value(LectureResponseCode.CREATED))
                .andExpect(jsonPath("$.message").value(LectureResponseMessage.CREATED))
                .andExpect(jsonPath("$.data.title").value("Java 1"));
    }

    @Test
    void updateLecture_returnsApiResponse() throws Exception {
        Long lectureId = 1L;
        LectureUpdateRequest request = new LectureUpdateRequest(
                "Updated Java",
                "updated",
                "updated.mp4",
                "updated.png",
                2,
                LectureStatus.INACTIVE
        );
        given(lectureCommandUseCase.updateLecture(any(UpdateLectureCommand.class)))
                .willReturn(createDetailResult(lectureId, "Updated Java"));

        mockMvc.perform(put("/api/v1/lectures/{lectureId}", lectureId)
                        .with(authentication(operatorUser(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(LectureResponseCode.UPDATED))
                .andExpect(jsonPath("$.message").value(LectureResponseMessage.UPDATED))
                .andExpect(jsonPath("$.data.title").value("Updated Java"));
    }

    @Test
    void deleteLecture_returnsNoContent() throws Exception {
        Long lectureId = 1L;

        mockMvc.perform(delete("/api/v1/lectures/{lectureId}", lectureId)
                        .with(authentication(operatorUser(10L))))
                .andExpect(status().isNoContent());

        verify(lectureCommandUseCase).deleteLecture(lectureId);
    }

    private Lecture createDetailResult(Long lectureId, String title) {
        return createLecture(lectureId, createCourse(1L), title);
    }

    private Course createCourse(Long courseId) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setInstructorId(10L);
        course.setTitle("Java");
        course.setDescription("course description");
        course.setThumbnailUrl("course.png");
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        return course;
    }

    private Lecture createLecture(Long lectureId, Course course, String title) {
        Lecture lecture = new Lecture();
        lecture.setLectureId(lectureId);
        lecture.setCourse(course);
        lecture.setTitle(title);
        lecture.setDescription("description");
        lecture.setVideoUrl("java-1.mp4");
        lecture.setThumbnailUrl("java-1.png");
        lecture.setStatus(LectureStatus.ACTIVE);
        lecture.setLectureOrder(1);
        lecture.setCreatedAt(LocalDateTime.now());
        lecture.setUpdatedAt(LocalDateTime.now());
        return lecture;
    }

    private Authentication operatorUser(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId, null, "ROLE_OPERATOR");
        authentication.setAuthenticated(true);
        return authentication;
    }
}
