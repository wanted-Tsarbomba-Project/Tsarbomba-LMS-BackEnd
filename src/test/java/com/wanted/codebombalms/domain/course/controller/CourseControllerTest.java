package com.wanted.codebombalms.domain.course.controller;

import com.wanted.codebombalms.domain.course.dto.request.CourseCreateRequest;
import com.wanted.codebombalms.domain.course.dto.request.CourseUpdateRequest;
import com.wanted.codebombalms.domain.course.dto.response.CourseDetailResponse;
import com.wanted.codebombalms.domain.course.dto.response.CourseResponse;
import com.wanted.codebombalms.domain.course.enums.CourseStatus;
import com.wanted.codebombalms.domain.course.service.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CourseController 웹 계층 테스트")
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    @Test
    @DisplayName("강좌 목록 조회 API가 정상 응답을 반환한다.")
    void 강좌_목록_조회_테스트() throws Exception {

        // given
        List<CourseResponse> response = List.of(
                new CourseResponse(1L, 10L, "Java 기초 강좌", "java.png", CourseStatus.ACTIVE),
                new CourseResponse(2L, 10L, "Spring 기초 강좌", "spring.png", CourseStatus.ACTIVE)
        );

        given(courseService.findAllCourses()).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("강좌 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].courseId").value(1L))
                .andExpect(jsonPath("$.data[0].title").value("Java 기초 강좌"))
                .andExpect(jsonPath("$.data[1].courseId").value(2L))
                .andExpect(jsonPath("$.data[1].title").value("Spring 기초 강좌"));
    }

    @Test
    @DisplayName("강좌 상세 조회 API가 정상 응답을 반환한다.")
    void 강좌_상세_조회_테스트() throws Exception {

        // given
        Long courseId = 1L;

        CourseDetailResponse response = new CourseDetailResponse(
                courseId,
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(courseService.findCourseById(courseId)).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/courses/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("강좌 상세 조회 성공"))
                .andExpect(jsonPath("$.data.courseId").value(courseId))
                .andExpect(jsonPath("$.data.title").value("Java 기초 강좌"))
                .andExpect(jsonPath("$.data.description").value("Java 기초 문법을 학습하는 강좌입니다."));
    }

    @Test
    @DisplayName("강좌 등록 API가 정상 응답을 반환한다.")
    void 강좌_등록_테스트() throws Exception {

        // given
        CourseCreateRequest request = new CourseCreateRequest(
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE
        );

        CourseDetailResponse response = new CourseDetailResponse(
                1L,
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(courseService.createCourse(any(CourseCreateRequest.class))).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("강좌 등록 성공"))
                .andExpect(jsonPath("$.data.courseId").value(1L))
                .andExpect(jsonPath("$.data.title").value("Java 기초 강좌"));
    }

    @Test
    @DisplayName("강좌 수정 API가 정상 응답을 반환한다.")
    void 강좌_수정_테스트() throws Exception {

        // given
        Long courseId = 1L;

        CourseUpdateRequest request = new CourseUpdateRequest(
                "수정된 Java 강좌",
                "수정된 강좌 설명입니다.",
                "updated-java.png",
                CourseStatus.ACTIVE
        );

        CourseDetailResponse response = new CourseDetailResponse(
                courseId,
                10L,
                "수정된 Java 강좌",
                "수정된 강좌 설명입니다.",
                "updated-java.png",
                CourseStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(courseService.updateCourse(eq(courseId), any(CourseUpdateRequest.class))).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/v1/courses/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("강좌 수정 성공"))
                .andExpect(jsonPath("$.data.courseId").value(courseId))
                .andExpect(jsonPath("$.data.title").value("수정된 Java 강좌"));
    }

    @Test
    @DisplayName("강좌 삭제 API가 204 상태 코드를 반환한다.")
    void 강좌_삭제_테스트() throws Exception {

        // given
        Long courseId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/courses/{courseId}", courseId)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(courseService).deleteCourse(courseId);
    }
}