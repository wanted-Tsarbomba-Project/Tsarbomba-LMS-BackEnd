package com.wanted.codebombalms.badge.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.badge.application.command.CreateBadgeCommand;
import com.wanted.codebombalms.badge.application.command.UpdateBadgeCommand;
import com.wanted.codebombalms.badge.application.usecase.AdminBadgeUseCase;
import com.wanted.codebombalms.badge.exception.BadgeErrorCode;
import com.wanted.codebombalms.badge.presentation.request.CreateBadgeRequest;
import com.wanted.codebombalms.badge.presentation.request.UpdateBadgeRequest;
import com.wanted.codebombalms.badge.presentation.response.BadgeResponse;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/badges")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
@Tag(name = "배지 관리", description = "운영자가 배지를 등록, 조회, 수정, 삭제하는 API")
public class AdminBadgeController {

    private final AdminBadgeUseCase adminBadgeUseCase;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "관리자 배지 목록 조회",
            description = "운영자가 등록한 배지 목록을 최신 등록순으로 조회합니다. 비활성 배지도 관리자 화면에서는 함께 확인할 수 있습니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getBadges() {
        var badges = adminBadgeUseCase.getBadges().stream()
                .map(BadgeResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "배지 목록 조회에 성공했습니다.",
                badges
        ));
    }

    @Operation(
            summary = "관리자 배지 상세 조회",
            description = "배지 수정 화면에서 사용할 배지 상세 정보를 조회합니다. 기존 이미지 URL, 지급 기준 포인트, 상태를 함께 반환합니다."
    )
    @GetMapping("/{badgeId}")
    public ResponseEntity<ApiResponse<BadgeResponse>> getBadge(
            @Parameter(description = "조회할 배지 ID", example = "1", required = true)
            @PathVariable Long badgeId
    ) {
        var badge = adminBadgeUseCase.getBadge(badgeId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "배지 상세 조회에 성공했습니다.",
                BadgeResponse.from(badge)
        ));
    }

    @Operation(
            summary = "배지 등록",
            description = "운영자가 배지 이름, 설명, 지급 기준 포인트와 배지 이미지를 multipart/form-data로 등록합니다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BadgeResponse>> createBadge(
            @Parameter(description = "배지 등록 정보 JSON")
            @RequestPart("request") String requestJson,
            @Parameter(description = "업로드할 배지 이미지 파일. jpg, jpeg, png, webp 형식을 사용합니다.")
            @RequestPart("badgeImage") MultipartFile badgeImage
    ) throws IOException {
        CreateBadgeRequest request = parseCreateBadgeRequest(requestJson);

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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "배지 등록에 성공했습니다.",
                        BadgeResponse.from(badge)
                ));
    }

    @Operation(
            summary = "배지 수정",
            description = "운영자가 배지 이름, 설명, 지급 기준 포인트, 상태를 수정합니다. badgeImage를 함께 보내면 기존 이미지를 새 이미지로 교체합니다."
    )
    @PatchMapping(
            value = "/{badgeId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<BadgeResponse>> updateBadge(
            @Parameter(description = "수정할 배지 ID", example = "1", required = true)
            @PathVariable Long badgeId,
            @Parameter(description = "배지 수정 정보 JSON")
            @RequestPart("request") String requestJson,
            @Parameter(description = "새 배지 이미지 파일. 이미지를 바꾸지 않으면 생략합니다.")
            @RequestPart(value = "badgeImage", required = false) MultipartFile badgeImage
    ) throws IOException {
        UpdateBadgeRequest request = parseUpdateBadgeRequest(requestJson);

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
                "배지 수정에 성공했습니다.",
                BadgeResponse.from(badge)
        ));
    }

    @Operation(
            summary = "배지 삭제",
            description = "배지를 즉시 물리 삭제하지 않고 소프트 삭제합니다. 삭제된 배지는 학생 화면에 노출되지 않으며, 보관 기간 이후 스케줄러가 하드 삭제합니다."
    )
    @DeleteMapping("/{badgeId}")
    public ResponseEntity<ApiResponse<Void>> deleteBadge(
            @Parameter(description = "삭제할 배지 ID", example = "1", required = true)
            @PathVariable Long badgeId
    ) {
        adminBadgeUseCase.deleteBadge(badgeId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "배지 삭제에 성공했습니다.",
                null
        ));
    }

    private CreateBadgeRequest parseCreateBadgeRequest(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, CreateBadgeRequest.class);
        } catch (JsonProcessingException e) {
            throw new ValidationException(
                    BadgeErrorCode.BADGE_INVALID_INPUT,
                    e
            );
        }
    }

    private UpdateBadgeRequest parseUpdateBadgeRequest(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, UpdateBadgeRequest.class);
        } catch (JsonProcessingException e) {
            throw new ValidationException(
                    BadgeErrorCode.BADGE_INVALID_INPUT,
                    e
            );
        }
    }
}
