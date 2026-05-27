package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.usecase.ChangeStudentLockUseCase;
import com.wanted.codebombalms.user.presentation.api.dto.request.ChangeStudentLockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User - Admin", description = "관리자 전용 학생 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ChangeStudentLockController {

    private final ChangeStudentLockUseCase changeStudentLockUseCase;

    @Operation(
            summary = "계정 정지/해제 (Admin)",
            description = "userId 대상 계정 잠금 상태 변경. locked=true 정지 + Refresh Token 전체 삭제(강제 로그아웃) / locked=false 해제."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "AUT-015 권한 없음")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USR-001 존재하지 않는 회원")
    @PatchMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changeLock(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeStudentLockRequest request
    ) {
        changeStudentLockUseCase.changeLock(userId, request.locked());

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.STUDENT_LOCK_CHANGED,
                UserResponseMessage.STUDENT_LOCK_CHANGED
        ));
    }
}
