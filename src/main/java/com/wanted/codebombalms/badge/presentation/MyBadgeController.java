package com.wanted.codebombalms.badge.presentation;

import com.wanted.codebombalms.badge.application.usecase.MyBadgeUseCase;
import com.wanted.codebombalms.badge.presentation.response.BadgeSyncResponse;
import com.wanted.codebombalms.badge.presentation.response.MyBadgeResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/badges/me")
@RequiredArgsConstructor
@Tag(name = "내 배지", description = "로그인 사용자의 배지 조회, 동기화, 대표 배지 설정 API")
public class MyBadgeController {

    private final MyBadgeUseCase myBadgeUseCase;

    @Operation(
            summary = "내 배지 목록 조회",
            description = "로그인 사용자가 획득한 배지 목록을 조회합니다. 대표 배지 여부도 함께 반환합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<MyBadgeResponse>>> getMyBadges(
            @AuthenticationPrincipal Long userId
    ) {
        var badges = myBadgeUseCase.getMyBadges(userId).stream()
                .map(MyBadgeResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "내 배지 목록 조회에 성공했습니다.",
                badges
        ));
    }

    @Operation(
            summary = "대표 배지 설정",
            description = "로그인 사용자가 이미 획득한 배지 중 하나를 대표 배지로 설정합니다. 기존 대표 배지는 자동 해제됩니다."
    )
    @PatchMapping("/{badgeId}/equip")
    public ResponseEntity<ApiResponse<MyBadgeResponse>> equipBadge(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "대표 배지로 설정할 배지 ID", example = "1", required = true)
            @PathVariable Long badgeId
    ) {
        var badge = myBadgeUseCase.equipBadge(userId, badgeId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "대표 배지 설정에 성공했습니다.",
                MyBadgeResponse.from(badge)
        ));
    }

    @Operation(
            summary = "내 배지 동기화",
            description = "현재 누적 포인트를 기준으로 획득 가능한 배지를 계산해 사용자에게 지급합니다. 포인트 지급 이벤트 누락 시 복구용으로도 사용할 수 있습니다."
    )
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<BadgeSyncResponse>> syncMyBadges(
            @AuthenticationPrincipal Long userId
    ) {
        var result = myBadgeUseCase.syncBadges(userId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "획득 가능한 배지 동기화에 성공했습니다.",
                BadgeSyncResponse.from(result)
        ));
    }
}
