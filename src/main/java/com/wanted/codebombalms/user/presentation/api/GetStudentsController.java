package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.query.StudentPageResult;
import com.wanted.codebombalms.user.application.usecase.GetStudentsUseCase;
import com.wanted.codebombalms.user.presentation.api.dto.response.StudentPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User - Admin", description = "관리자 전용 학생 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class GetStudentsController {

    private final GetStudentsUseCase getStudentsUseCase;

    @Operation(
            summary = "학생 전체 조회 (Admin)",
            description = "역할 STUDENT 인 회원 목록 페이지 조회 (가입 최신순). 관리자 권한 필요."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "AUT-015 권한 없음")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentPageResponse>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        StudentPageResult result = getStudentsUseCase.getStudents(page, size);

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.STUDENTS_RETRIEVED,
                UserResponseMessage.STUDENTS_RETRIEVED,
                StudentPageResponse.from(result)
        ));
    }
}
