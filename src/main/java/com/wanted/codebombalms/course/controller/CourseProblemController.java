package com.wanted.codebombalms.course.controller;

import com.wanted.codebombalms.course.application.usecase.CourseProblemCommandUseCase;
import com.wanted.codebombalms.course.application.usecase.CourseProblemQueryUseCase;
import com.wanted.codebombalms.course.presentation.api.request.CourseProblemSetConfigureRequest;
import com.wanted.codebombalms.course.presentation.api.response.CourseProblemSetResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CourseProblemController {

    private static final Logger log = LoggerFactory.getLogger(CourseProblemController.class);

    private final CourseProblemCommandUseCase courseProblemCommandUseCase;
    private final CourseProblemQueryUseCase courseProblemQueryUseCase;

    @GetMapping("/courses/{courseId}/problem-sets")
    public ResponseEntity<ApiResponse<?>> findProblemSetsByCourse(@PathVariable Long courseId) {
        log.info("[CourseProblemController] find course problem sets - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                courseProblemQueryUseCase.findProblemSetsByCourse(courseId)
                        .stream()
                        .map(CourseProblemSetResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/lectures/{lectureId}/problem-sets")
    public ResponseEntity<ApiResponse<?>> findProblemSetsByLecture(@PathVariable Long lectureId) {
        log.info("[CourseProblemController] find lecture problem sets - lectureId: {}", lectureId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                courseProblemQueryUseCase.findProblemSetsByLecture(lectureId)
                        .stream()
                        .map(CourseProblemSetResponse::from)
                        .toList()
        ));
    }

    @PutMapping("/courses/{courseId}/problem-sets")
    public ResponseEntity<ApiResponse<?>> configureProblemSets(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseProblemSetConfigureRequest request
    ) {
        log.info("[CourseProblemController] configure course problem sets - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.UPDATED,
                CourseResponseMessage.UPDATED,
                courseProblemCommandUseCase.configureProblemSets(request.toCommand(courseId))
                        .stream()
                        .map(CourseProblemSetResponse::from)
                        .toList()
        ));
    }
}
