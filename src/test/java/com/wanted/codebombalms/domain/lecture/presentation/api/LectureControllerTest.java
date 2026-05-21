package com.wanted.codebombalms.domain.lecture.presentation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureUpdateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.response.LectureDetailResponse;
import com.wanted.codebombalms.domain.lecture.presentation.api.response.LectureResponse;
import com.wanted.codebombalms.domain.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.domain.lecture.application.service.LectureService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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

@WebMvcTest(LectureController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("LectureController 웹 계층 테스트")
public class LectureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LectureService lectureService;

    @Test
    @DisplayName("특정 강좌의 강의 목록 조회 API가 정상 응답을 반환한다.")
    void 강의_목록_조회_테스트() throws Exception {

        // given
        Long courseId = 1L;

        List<LectureResponse> response = List.of(
                new LectureResponse(1L, courseId, 10L, "Java 1강", "java-1.png", LectureStatus.ACTIVE, 1),
                new LectureResponse(2L, courseId, 10L, "Java 2강", "java-2.png", LectureStatus.ACTIVE, 2)
        );

        given(lectureService.findLecturesByCourseId(courseId)).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/courses/{courseId}/lectures", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("강의 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].lectureId").value(1L))
                .andExpect(jsonPath("$.data[0].title").value("Java 1강"))
                .andExpect(jsonPath("$.data[0].lectureOrder").value(1L))
                .andExpect(jsonPath("$.data[1].lectureId").value(2L))
                .andExpect(jsonPath("$.data[1].title").value("Java 2강"))
                .andExpect(jsonPath("$.data[1].lectureOrder").value(2L));
    }

    @Test
    @DisplayName("강의 상세 조회 API가 정상 응답을 반환한다.")
    void 강의_상세_조회_테스트() throws Exception {

        // given
        Long lectureId = 1L;

        LectureDetailResponse response = new LectureDetailResponse(
                lectureId,
                1L,
                10L,
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                LectureStatus.ACTIVE,
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(lectureService.findLectureById(lectureId)).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/lectures/{lectureId}", lectureId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("강의 상세 조회 성공"))
                .andExpect(jsonPath("$.data.lectureId").value(lectureId))
                .andExpect(jsonPath("$.data.title").value("Java 1강"))
                .andExpect(jsonPath("$.data.description").value("Java 기본 문법을 학습하는 강의입니다."))
                .andExpect(jsonPath("$.data.videoUrl").value("java-1.mp4"));
    }

    @Test
    @DisplayName("강의 등록 API가 정상 응답을 반환한다.")
    void 강의_등록_테스트() throws Exception {

        // given
        Long courseId = 1L;

        LectureCreateRequest request = new LectureCreateRequest(
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                1,
                LectureStatus.ACTIVE
        );

        LectureDetailResponse response = new LectureDetailResponse(
                1L,
                courseId,
                10L,
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                LectureStatus.ACTIVE,
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(lectureService.createLecture(eq(courseId), any(LectureCreateRequest.class))).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/courses/{courseId}/lectures", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("강의 등록 성공"))
                .andExpect(jsonPath("$.data.lectureId").value(1L))
                .andExpect(jsonPath("$.data.courseId").value(courseId))
                .andExpect(jsonPath("$.data.title").value("Java 1강"));
    }

    @Test
    @DisplayName("강의 수정 API가 정상 응답을 반환한다.")
    void 강의_수정_테스트() throws Exception {

        // given
        Long lectureId = 1L;

        LectureUpdateRequest request = new LectureUpdateRequest(
                "수정된 Java 1강",
                "수정된 강의 설명입니다.",
                "updated-java-1.mp4",
                "updated-java-1.png",
                1,
                LectureStatus.INACTIVE
        );

        LectureDetailResponse response = new LectureDetailResponse(
                lectureId,
                1L,
                10L,
                "수정된 Java 1강",
                "수정된 강의 설명입니다.",
                "updated-java-1.mp4",
                "updated-java-1.png",
                LectureStatus.INACTIVE,
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(lectureService.updateLecture(eq(lectureId), any(LectureUpdateRequest.class))).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/v1/lectures/{lectureId}", lectureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("강의 수정 성공"))
                .andExpect(jsonPath("$.data.lectureId").value(lectureId))
                .andExpect(jsonPath("$.data.title").value("수정된 Java 1강"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("강의 삭제 API가 204 상태 코드를 반환한다.")
    void 강의_삭제_테스트() throws Exception {

        // given
        Long lectureId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/lectures/{lectureId}", lectureId)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(lectureService).deleteLecture(lectureId);
    }
}