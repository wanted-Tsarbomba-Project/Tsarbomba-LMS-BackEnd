package com.wanted.codebombalms.domain.lecture.presentation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.domain.lecture.application.service.LectureService;
import com.wanted.codebombalms.domain.lecture.controller.LectureController;
import com.wanted.codebombalms.domain.lecture.controller.LectureResponseCode;
import com.wanted.codebombalms.domain.lecture.controller.LectureResponseMessage;
import com.wanted.codebombalms.domain.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureUpdateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.response.LectureDetailResponse;
import com.wanted.codebombalms.domain.lecture.presentation.api.response.LectureResponse;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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
    private LectureService lectureService;

    @Test
    void findLecturesByCourseId_returnsApiResponse() throws Exception {
        Long courseId = 1L;
        given(lectureService.findLecturesByCourseId(courseId)).willReturn(List.of(
                new LectureResponse(1L, courseId, 10L, "Java 1", "java-1.png", LectureStatus.ACTIVE, 1)
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
        given(lectureService.findLectureById(lectureId)).willReturn(createDetailResponse(lectureId, "Java 1"));

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
        given(lectureService.createLecture(eq(courseId), any(LectureCreateRequest.class)))
                .willReturn(createDetailResponse(1L, "Java 1"));

        mockMvc.perform(post("/api/v1/courses/{courseId}/lectures", courseId)
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
        given(lectureService.updateLecture(eq(lectureId), any(LectureUpdateRequest.class)))
                .willReturn(createDetailResponse(lectureId, "Updated Java"));

        mockMvc.perform(put("/api/v1/lectures/{lectureId}", lectureId)
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

        mockMvc.perform(delete("/api/v1/lectures/{lectureId}", lectureId))
                .andExpect(status().isNoContent());

        verify(lectureService).deleteLecture(lectureId);
    }

    private LectureDetailResponse createDetailResponse(Long lectureId, String title) {
        return new LectureDetailResponse(
                lectureId,
                1L,
                10L,
                title,
                "description",
                "java-1.mp4",
                "java-1.png",
                LectureStatus.ACTIVE,
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
