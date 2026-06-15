package com.wanted.codebombalms.course.controller;

import com.wanted.codebombalms.admin.permission.application.service.AdminPermissionCheckService;
import com.wanted.codebombalms.course.presentation.api.CourseProblemController;
import com.wanted.codebombalms.course.presentation.api.CourseResponseCode;
import com.wanted.codebombalms.lecture.application.command.ConfigureLectureProblemSetsCommand;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetCommandUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetQueryUseCase;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
    private LectureProblemSetQueryUseCase lectureProblemSetQueryUseCase;

    @MockitoBean
    private LectureProblemSetCommandUseCase lectureProblemSetCommandUseCase;

    @MockitoBean
    private AdminPermissionCheckService adminPermissionCheckService;

    @Test
    void findProblemSetsByCourseReturnsApiResponse() throws Exception {
        given(lectureProblemSetQueryUseCase.findProblemSetsByCourse(101L)).willReturn(List.of(
                LectureProblemSet.restore(6001L, 101L, 1001L, 2002L, LectureProblemSetRole.MAIN, 1),
                LectureProblemSet.restore(6002L, 101L, null, 2003L, LectureProblemSetRole.FINAL, 1)
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
    void findProblemSetsByLectureReturnsApiResponse() throws Exception {
        given(lectureProblemSetQueryUseCase.findProblemSetsByLecture(101L)).willReturn(List.of(
                LectureProblemSet.restore(6001L, 101L, 101L, 2002L, LectureProblemSetRole.MAIN, 1)
        ));

        mockMvc.perform(get("/api/v1/lectures/{lectureId}/problem-sets", 101L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(CourseResponseCode.RETRIEVED))
                .andExpect(jsonPath("$.data[0].courseProblemSetId").value(6001L))
                .andExpect(jsonPath("$.data[0].problemSetId").value(2002L))
                .andExpect(jsonPath("$.data[0].lectureId").value(101L));
    }

    @Test
    void configureProblemSetsReturnsApiResponse() throws Exception {
        given(lectureProblemSetCommandUseCase.configureProblemSets(any(ConfigureLectureProblemSetsCommand.class)))
                .willReturn(List.of(
                        LectureProblemSet.restore(6001L, 101L, 101L, 2002L, LectureProblemSetRole.MAIN, 1),
                        LectureProblemSet.restore(6002L, 101L, null, 2003L, LectureProblemSetRole.FINAL, 1)
                ));

        String request = """
                {
                  "problemSets": [
                    {
                      "lectureId": 101,
                      "problemSetId": 2002,
                      "role": "MAIN",
                      "displayOrder": 1
                    },
                    {
                      "lectureId": null,
                      "problemSetId": 2003,
                      "role": "FINAL",
                      "displayOrder": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(put("/api/v1/courses/{courseId}/problem-sets", 101L)
                        .with(authentication(operatorUser(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(CourseResponseCode.UPDATED))
                .andExpect(jsonPath("$.data[0].role").value("MAIN"))
                .andExpect(jsonPath("$.data[1].role").value("FINAL"));

        ArgumentCaptor<ConfigureLectureProblemSetsCommand> commandCaptor =
                ArgumentCaptor.forClass(ConfigureLectureProblemSetsCommand.class);
        verify(lectureProblemSetCommandUseCase).configureProblemSets(commandCaptor.capture());

        ConfigureLectureProblemSetsCommand command = commandCaptor.getValue();
        assertEquals(101L, command.courseId());
        assertEquals(2, command.problemSets().size());
        assertEquals(101L, command.problemSets().get(0).lectureId());
        assertEquals(2002L, command.problemSets().get(0).problemSetId());
        assertEquals(LectureProblemSetRole.MAIN, command.problemSets().get(0).role());
        assertEquals(1, command.problemSets().get(0).displayOrder());
        assertNull(command.problemSets().get(1).lectureId());
        assertEquals(2003L, command.problemSets().get(1).problemSetId());
        assertEquals(LectureProblemSetRole.FINAL, command.problemSets().get(1).role());
        assertEquals(1, command.problemSets().get(1).displayOrder());
    }

    @Test
    void configureProblemSetsRejectsNonPositiveDisplayOrder() throws Exception {
        String request = """
                {
                  "problemSets": [
                    {
                      "lectureId": 101,
                      "problemSetId": 2002,
                      "role": "MAIN",
                      "displayOrder": 0
                    }
                  ]
                }
                """;

        mockMvc.perform(put("/api/v1/courses/{courseId}/problem-sets", 101L)
                        .with(authentication(operatorUser(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    private Authentication operatorUser(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId, null, "ROLE_OPERATOR");
        authentication.setAuthenticated(true);
        return authentication;
    }
}
