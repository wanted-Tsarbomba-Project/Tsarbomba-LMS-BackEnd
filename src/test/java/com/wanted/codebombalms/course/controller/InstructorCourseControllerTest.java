package com.wanted.codebombalms.course.controller;

import com.wanted.codebombalms.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InstructorCourseController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("InstructorCourseController web test")
class InstructorCourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseQueryUseCase courseQueryUseCase;

    @Test
    void findCoursesByInstructor_returnsApiResponse() throws Exception {
        Long instructorId = 10L;
        given(courseQueryUseCase.findCoursesByInstructor(instructorId)).willReturn(List.of(
                createCourse(1L, instructorId, "Java"),
                createCourse(2L, instructorId, "Spring")
        ));

        mockMvc.perform(get("/api/v1/instructors/{instructorId}/courses", instructorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(CourseResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.message").value(CourseResponseMessage.RETRIEVED))
                .andExpect(jsonPath("$.data[0].courseId").value(1L))
                .andExpect(jsonPath("$.data[0].instructorId").value(instructorId))
                .andExpect(jsonPath("$.data[0].title").value("Java"))
                .andExpect(jsonPath("$.data[1].title").value("Spring"));
    }

    private Course createCourse(Long courseId, Long instructorId, String title) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setInstructorId(instructorId);
        course.setCourseCategoryId(1L);
        course.setCourseCategoryName("Python");
        course.setTitle(title);
        course.setDescription("description");
        course.setThumbnailUrl("java.png");
        course.setStatus(CourseStatus.ACTIVE);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        return course;
    }
}
