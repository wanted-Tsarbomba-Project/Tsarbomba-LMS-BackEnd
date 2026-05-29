package com.wanted.codebombalms.badge.presentation;

import com.wanted.codebombalms.badge.application.command.CreateBadgeCommand;
import com.wanted.codebombalms.badge.application.command.UpdateBadgeCommand;
import com.wanted.codebombalms.badge.application.usecase.AdminBadgeUseCase;
import com.wanted.codebombalms.badge.presentation.request.CreateBadgeRequest;
import com.wanted.codebombalms.badge.presentation.request.UpdateBadgeRequest;
import com.wanted.codebombalms.badge.presentation.response.BadgeResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/badges")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class AdminBadgeController {

    private final AdminBadgeUseCase adminBadgeUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getBadges() {
        var badges = adminBadgeUseCase.getBadges().stream()
                .map(BadgeResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "뱃지 목록 조회에 성공했습니다.",
                badges
        ));
    }

    @GetMapping("/{badgeId}")
    public ResponseEntity<ApiResponse<BadgeResponse>> getBadge(
            @PathVariable Long badgeId
    ) {
        var badge = adminBadgeUseCase.getBadge(badgeId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "뱃지 상세 조회에 성공했습니다.",
                BadgeResponse.from(badge)
        ));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BadgeResponse>> createBadge(
            @RequestPart("request") CreateBadgeRequest request,
            @RequestPart("badgeImage") MultipartFile badgeImage
    ) {
        var command = new CreateBadgeCommand(
                request.badgeName(),
                request.description(),
                request.requiredPoint(),
                badgeImage.getOriginalFilename(),
                badgeImage.getContentType(),
                badgeImage.getSize(),
                badgeImage.getBytes()
        );


        var badge = adminBadgeUseCase.createBadge(command);

        return ResponseEntity.ok(ApiResponse.created(
                ApiResponseCode.CREATED,
                "뱃지 등록에 성공했습니다.",
                BadgeResponse.from(badge)
        ));
    }

    @PatchMapping(
            value = "/{badgeId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<BadgeResponse>> updateBadge(
            @PathVariable Long badgeId,
            @RequestPart("request") UpdateBadgeRequest request,
            @RequestPart(value = "badgeImage", required = false) MultipartFile badgeImage
    ) {
        var command = new UpdateBadgeCommand(
                request.badgeName(),
                request.description(),
                request.requiredPoint(),
                request.status(),
                badgeImage != null ? badgeImage.getOriginalFilename() : null,
                badgeImage != null ? badgeImage.getContentType() : null,
                badgeImage != null ? badgeImage.getSize() : 0,
                badgeImage != null ? badgeImage.getBytes() : null
        );

        var badge = adminBadgeUseCase.updateBadge(badgeId, command);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "뱃지 수정에 성공했습니다.",
                BadgeResponse.from(badge)
        ));
    }

    @DeleteMapping("/{badgeId}")
    public ResponseEntity<ApiResponse<Void>> deleteBadge(
            @PathVariable Long badgeId
    ) {
        adminBadgeUseCase.deleteBadge(badgeId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "뱃지 삭제에 성공했습니다.",
                null
        ));
    }
}
