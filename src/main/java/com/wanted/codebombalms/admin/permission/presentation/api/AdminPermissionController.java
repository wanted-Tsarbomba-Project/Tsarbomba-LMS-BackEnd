package com.wanted.codebombalms.admin.permission.presentation.api;

import com.wanted.codebombalms.admin.permission.application.query.AdminAccountPageResult;
import com.wanted.codebombalms.admin.permission.application.query.AdminAccountPermissionResult;
import com.wanted.codebombalms.admin.permission.application.usecase.GetAdminAccountsUseCase;
import com.wanted.codebombalms.admin.permission.application.usecase.ToggleAdminPermissionUseCase;
import com.wanted.codebombalms.admin.permission.presentation.api.request.AdminPermissionToggleRequest;
import com.wanted.codebombalms.admin.permission.presentation.api.response.AdminAccountListResponse;
import com.wanted.codebombalms.admin.permission.presentation.api.response.AdminAccountPermissionResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// master 전용 admin 계정 목록 조회와 권한 부여/회수 API를 제공한다.
@Tag(name = "Admin Permission", description = "최고 관리자 전용 admin 계정 권한 관리")
@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final GetAdminAccountsUseCase getAdminAccountsUseCase;
    private final ToggleAdminPermissionUseCase toggleAdminPermissionUseCase;

    // master만 admin 계정 목록과 user/rule 권한 상태를 조회한다.
    @Operation(summary = "관리자 계정 목록 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADM-AUTH-004: 최고 관리자만 접근할 수 있습니다.")
    })
    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<AdminAccountListResponse>> findAdminAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        AdminAccountPageResult result = getAdminAccountsUseCase.getAdminAccounts(
                keyword,
                page,
                size
        );

        return ResponseEntity.ok(ApiResponse.success(
                AdminPermissionResponseCode.ACCOUNTS_RETRIEVED,
                AdminPermissionResponseMessage.ACCOUNTS_RETRIEVED,
                AdminAccountListResponse.from(result)
        ));
    }

    // master만 특정 admin 계정의 단일 권한을 부여하거나 회수한다.
    @Operation(summary = "관리자 계정 단일 권한 부여/회수")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ADM-AUTH-005: 관리자 권한 수정 요청이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADM-AUTH-004: 최고 관리자만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "ADM-AUTH-006: 관리자 계정을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "ADM-AUTH-007: 관리자 권한 상태가 올바르지 않습니다.")
    })
    @PatchMapping("/{adminUserId}/permissions")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<AdminAccountPermissionResponse>> toggleAdminPermission(
            @PathVariable Long adminUserId,
            @AuthenticationPrincipal Long masterUserId,
            @RequestBody AdminPermissionToggleRequest request
    ) {
        AdminAccountPermissionResult result = toggleAdminPermissionUseCase.togglePermission(
                request.toCommand(adminUserId, masterUserId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                AdminPermissionResponseCode.PERMISSION_UPDATED,
                AdminPermissionResponseMessage.PERMISSION_UPDATED,
                AdminAccountPermissionResponse.from(result)
        ));
    }
}
