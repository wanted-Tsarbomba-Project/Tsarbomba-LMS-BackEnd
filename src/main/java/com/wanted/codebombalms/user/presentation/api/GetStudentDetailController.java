package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.query.StudentDetail;
import com.wanted.codebombalms.user.application.usecase.GetStudentDetailUseCase;
import com.wanted.codebombalms.user.presentation.api.response.StudentDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User - Admin", description = "관리자 전용 학생 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class GetStudentDetailController {

    private final GetStudentDetailUseCase getStudentDetailUseCase;

    @Operation(
            summary = "학생 상세 조회 (Admin)",
            description = "userId 로 특정 회원 상세 정보 조회 (모든 필드). 관리자 권한 필요."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "AUT-015 권한 없음")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USR-001 존재하지 않는 회원")
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> getStudentDetail(
            @PathVariable Long userId
    ) {
        StudentDetail detail = getStudentDetailUseCase.getStudentDetail(userId);

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.STUDENT_DETAIL_RETRIEVED,
                UserResponseMessage.STUDENT_DETAIL_RETRIEVED,
                StudentDetailResponse.from(detail)
        ));
    }
}
