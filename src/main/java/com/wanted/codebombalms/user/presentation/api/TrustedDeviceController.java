package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.auth.presentation.api.support.DeviceFingerprintResolver;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.usecase.GetTrustedDevicesUseCase;
import com.wanted.codebombalms.user.application.usecase.RemoveTrustedDeviceUseCase;
import com.wanted.codebombalms.user.presentation.api.response.TrustedDeviceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User - 신뢰 기기", description = "적응형 인증 신뢰 기기 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class TrustedDeviceController {

    private final GetTrustedDevicesUseCase getTrustedDevicesUseCase;
    private final RemoveTrustedDeviceUseCase removeTrustedDeviceUseCase;
    private final DeviceFingerprintResolver deviceFingerprintResolver;

    @Operation(summary = "신뢰 기기 목록 조회", description = "로그인 사용자의 신뢰 기기 목록 조회 (현재 기기 표시 포함).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다 (미로그인)")
    @GetMapping("/me/trusted-devices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TrustedDeviceResponse>>> getTrustedDevices(
            @AuthenticationPrincipal Long userId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String currentFp = deviceFingerprintResolver.resolve(request, response);

        List<TrustedDeviceResponse> devices = getTrustedDevicesUseCase.getTrustedDevices(userId).stream()
                .map(device -> TrustedDeviceResponse.from(device, currentFp))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.TRUSTED_DEVICES_RETRIEVED,
                UserResponseMessage.TRUSTED_DEVICES_RETRIEVED,
                devices
        ));
    }

    @Operation(summary = "신뢰 기기 해제", description = "신뢰 기기를 해제한다. 해제된 기기는 다음 로그인 시 추가 인증(step-up)을 다시 요구한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "해제 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다 (미로그인)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USR-012 신뢰 기기를 찾을 수 없습니다")
    @DeleteMapping("/me/trusted-devices/{deviceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeTrustedDevice(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long deviceId
    ) {
        removeTrustedDeviceUseCase.remove(userId, deviceId);

        return ResponseEntity.ok(ApiResponse.<Void>success(
                UserResponseCode.TRUSTED_DEVICE_REMOVED,
                UserResponseMessage.TRUSTED_DEVICE_REMOVED,
                null
        ));
    }
}
