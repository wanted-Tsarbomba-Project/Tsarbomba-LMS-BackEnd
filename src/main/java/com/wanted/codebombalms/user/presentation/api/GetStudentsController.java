package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.query.StudentPageResult;
import com.wanted.codebombalms.user.application.usecase.GetStudentsUseCase;
import com.wanted.codebombalms.user.presentation.api.response.StudentPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User - Admin", description = "관리자 전용 학생 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class GetStudentsController {

    private final GetStudentsUseCase getStudentsUseCase;

    @Operation(
            summary = "학생 전체 조회 (Admin)",
            description = "역할 STUDENT 인 회원 목록 페이지 조회 (가입 최신순). "
                    + "keyword 전달 시 이름 기준 중간 매칭 검색 (대소문자 무시, 최대 20자). 관리자 권한 필요."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "페이지 파라미터 범위 오류")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "AUT-015 권한 없음")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentPageResponse>> getStudents(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Size(max = 20) String keyword
    ) {
        StudentPageResult result = getStudentsUseCase.getStudents(page, size, keyword);

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.STUDENTS_RETRIEVED,
                UserResponseMessage.STUDENTS_RETRIEVED,
                StudentPageResponse.from(result)
        ));
    }
}
