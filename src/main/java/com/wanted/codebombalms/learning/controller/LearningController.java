package com.wanted.codebombalms.learning.controller;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.learning.application.command.RecordLectureProgressCommand;
import com.wanted.codebombalms.learning.application.usecase.AdminLearningProgressQueryUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressCommandUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressQueryUseCase;
import com.wanted.codebombalms.learning.presentation.api.request.LectureProgressRequest;
import com.wanted.codebombalms.learning.presentation.api.response.LectureProgressResponse;
import com.wanted.codebombalms.learning.presentation.api.response.StudentLearningProgressResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LearningController {

    private final LectureProgressCommandUseCase lectureProgressCommandUseCase;
    private final LectureProgressQueryUseCase lectureProgressQueryUseCase;
    private final AdminLearningProgressQueryUseCase adminLearningProgressQueryUseCase;

    @PatchMapping("/lectures/{lectureId}/progress")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> recordLectureProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lectureId,
            @Valid @RequestBody LectureProgressRequest request
    ) {
        LectureProgressResponse response = LectureProgressResponse.from(
                lectureProgressCommandUseCase.recordProgress(new RecordLectureProgressCommand(
                        userId,
                        lectureId,
                        request.completed()
                ))
        );

        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.UPDATED,
                LearningResponseMessage.UPDATED,
                response
        ));
    }

    @GetMapping("/lectures/{lectureId}/progress")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> findLectureProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lectureId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                LectureProgressResponse.from(lectureProgressQueryUseCase.findProgress(userId, lectureId))
        ));
    }

    @GetMapping("/courses/{courseId}/learning-progress")
    public ResponseEntity<ApiResponse<List<StudentLearningProgressResponse>>> findStudentLearningProgresses(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                adminLearningProgressQueryUseCase.findStudentProgresses(courseId)
                        .stream()
                        .map(StudentLearningProgressResponse::from)
                        .toList()
        ));
    }
}
