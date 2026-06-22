package com.wanted.codebombalms.lecture.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.admin.permission.application.service.AdminPermissionCheckService;
import com.wanted.codebombalms.lecture.application.command.CreateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UpdateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UploadLectureMaterialCommand;
import com.wanted.codebombalms.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureMaterialUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.lecture.presentation.api.LectureController;
import com.wanted.codebombalms.lecture.presentation.api.LectureResponseCode;
import com.wanted.codebombalms.lecture.presentation.api.LectureResponseMessage;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.lecture.presentation.api.request.LectureUpdateRequest;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private LectureMaterialUseCase lectureMaterialUseCase;

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
        given(lectureQueryUseCase.findLectureByIdForLearning(eq(lectureId), isNull(), eq(false)))
                .willReturn(createDetailResult(lectureId, "Java 1"));

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
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
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
                "https://youtu.be/dQw4w9WgXcQ",
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

    @Test
    void uploadMaterial_returnsApiResponse() throws Exception {
        Long lectureId = 1L;
        MockMultipartFile material = new MockMultipartFile(
                "material",
                "guide.pdf",
                "application/pdf",
                "pdf".getBytes()
        );

        given(lectureMaterialUseCase.uploadMaterial(any(UploadLectureMaterialCommand.class)))
                .willReturn(createMaterial(10L, lectureId));

        mockMvc.perform(multipart("/api/v1/lectures/{lectureId}/materials", lectureId)
                        .file(material)
                        .with(authentication(operatorUser(10L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(LectureResponseCode.MATERIAL_UPLOADED))
                .andExpect(jsonPath("$.message").value(LectureResponseMessage.MATERIAL_UPLOADED))
                .andExpect(jsonPath("$.data.originalFileName").value("guide.pdf"));
    }

    @Test
    void findMaterials_returnsApiResponse() throws Exception {
        Long lectureId = 1L;
        given(lectureMaterialUseCase.findMaterials(lectureId))
                .willReturn(List.of(createMaterial(10L, lectureId)));

        mockMvc.perform(get("/api/v1/lectures/{lectureId}/materials", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LectureResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data[0].lectureMaterialId").value(10L));
    }

    @Test
    void issueMaterialDownloadUrl_returnsApiResponse() throws Exception {
        Long lectureMaterialId = 10L;
        given(lectureMaterialUseCase.issueDownloadUrl(any(), any(), anyBoolean()))
                .willReturn("https://storage.googleapis.com/codebombalms/lecture_materials/guide.pdf?signature=test");

        mockMvc.perform(post("/api/v1/lecture-materials/{lectureMaterialId}/download-url", lectureMaterialId)
                        .with(authentication(studentUser(20L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(LectureResponseCode.MATERIAL_DOWNLOAD_URL_ISSUED))
                .andExpect(jsonPath("$.data.downloadUrl").value("https://storage.googleapis.com/codebombalms/lecture_materials/guide.pdf?signature=test"));

        verify(lectureMaterialUseCase).issueDownloadUrl(eq(lectureMaterialId), isNull(), eq(false));
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
        lecture.setVideoUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        lecture.setThumbnailUrl("java-1.png");
        lecture.setStatus(LectureStatus.ACTIVE);
        lecture.setLectureOrder(1);
        lecture.setCreatedAt(LocalDateTime.now());
        lecture.setUpdatedAt(LocalDateTime.now());
        return lecture;
    }

    private LectureMaterial createMaterial(Long lectureMaterialId, Long lectureId) {
        return LectureMaterial.restore(
                lectureMaterialId,
                lectureId,
                "guide.pdf",
                "stored-guide.pdf",
                "lecture_materials/stored-guide.pdf",
                "application/pdf",
                3L,
                LocalDateTime.now(),
                null
        );
    }

    private Authentication operatorUser(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId, null, "ROLE_OPERATOR");
        authentication.setAuthenticated(true);
        return authentication;
    }

    private Authentication studentUser(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId, null, "ROLE_STUDENT");
        authentication.setAuthenticated(true);
        return authentication;
    }
}
