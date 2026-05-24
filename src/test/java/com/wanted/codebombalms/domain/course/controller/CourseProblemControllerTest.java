package com.wanted.codebombalms.domain.course.controller;

import com.wanted.codebombalms.course.application.command.ConfigureCourseProblemSetsCommand;
import com.wanted.codebombalms.course.application.usecase.CourseProblemCommandUseCase;
import com.wanted.codebombalms.course.application.usecase.CourseProblemQueryUseCase;
import com.wanted.codebombalms.course.controller.CourseProblemController;
import com.wanted.codebombalms.course.controller.CourseResponseCode;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.course.domain.model.CourseProblemStep;
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
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseProblemController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CourseProblemController web test")
class CourseProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseProblemQueryUseCase courseProblemQueryUseCase;

    @MockitoBean
    private CourseProblemCommandUseCase courseProblemCommandUseCase;

    @Test
    void findProblemSetsByCourseReturnsApiResponse() throws Exception {
        given(courseProblemQueryUseCase.findProblemSetsByCourse(101L)).willReturn(List.of(
                CourseProblemSet.restore(6001L, 101L, 2002L, CourseProblemSetRole.MAIN),
                CourseProblemSet.restore(6002L, 101L, 2003L, CourseProblemSetRole.FINAL)
        ));

        mockMvc.perform(get("/api/v1/courses/{courseId}/problem-sets", 101L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(CourseResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data[0].courseProblemSetId").value(6001L))
                .andExpect(jsonPath("$.data[0].role").value("MAIN"))
                .andExpect(jsonPath("$.data[1].role").value("FINAL"));
    }

    @Test
    void findProblemsByLectureReturnsApiResponse() throws Exception {
        given(courseProblemQueryUseCase.findProblemsByLecture(101L)).willReturn(List.of(
                CourseProblemStep.restore(6001L, 6001L, 2004L, 101L, 1L)
        ));

        mockMvc.perform(get("/api/v1/lectures/{lectureId}/problems", 101L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(CourseResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data[0].courseProblemStepId").value(6001L))
                .andExpect(jsonPath("$.data[0].problemId").value(2004L))
                .andExpect(jsonPath("$.data[0].lectureId").value(101L));
    }

    @Test
    void configureProblemSetsReturnsApiResponse() throws Exception {
        given(courseProblemCommandUseCase.configureProblemSets(any(ConfigureCourseProblemSetsCommand.class)))
                .willReturn(List.of(
                        CourseProblemSet.restore(6001L, 101L, 2002L, CourseProblemSetRole.MAIN),
                        CourseProblemSet.restore(6002L, 101L, 2003L, CourseProblemSetRole.FINAL)
                ));

        String request = """
                {
                  "problemSets": [
                    {
                      "problemSetId": 2002,
                      "role": "MAIN",
                      "steps": [
                        { "problemId": 2004, "lectureId": 101, "stepOrder": 1 }
                      ]
                    },
                    {
                      "problemSetId": 2003,
                      "role": "FINAL",
                      "steps": [
                        { "problemId": 2007, "lectureId": null, "stepOrder": 1 }
                      ]
                    }
                  ]
                }
                """;

        mockMvc.perform(put("/api/v1/courses/{courseId}/problem-sets", 101L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(CourseResponseCode.UPDATED))
                .andExpect(jsonPath("$.data[0].role").value("MAIN"))
                .andExpect(jsonPath("$.data[1].role").value("FINAL"));
    }
}
