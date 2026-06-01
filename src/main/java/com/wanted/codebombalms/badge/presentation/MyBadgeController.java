package com.wanted.codebombalms.badge.presentation;

import com.wanted.codebombalms.badge.application.usecase.MyBadgeUseCase;
import com.wanted.codebombalms.badge.presentation.response.BadgeSyncResponse;
import com.wanted.codebombalms.badge.presentation.response.MyBadgeResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/badges/me")
@RequiredArgsConstructor
public class MyBadgeController {

    private final MyBadgeUseCase myBadgeUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MyBadgeResponse>>> getMyBadges(
            @AuthenticationPrincipal Long userId
    ) {
        var badges = myBadgeUseCase.getMyBadges(userId).stream()
                .map(MyBadgeResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "내 뱃지 목록 조회에 성공했습니다.",
                badges
        ));
    }

    @PatchMapping("/{badgeId}/equip")
    public ResponseEntity<ApiResponse<MyBadgeResponse>> equipBadge(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long badgeId
    ) {
        var badge = myBadgeUseCase.equipBadge(userId, badgeId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "대표 뱃지 설정에 성공했습니다.",
                MyBadgeResponse.from(badge)
        ));
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<BadgeSyncResponse>> syncMyBadges(
            @AuthenticationPrincipal Long userId
    ) {
        var result = myBadgeUseCase.syncBadges(userId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "획득 가능한 뱃지 동기화에 성공했습니다.",
                BadgeSyncResponse.from(result)
        ));
    }
}
