package com.wanted.codebombalms.badge.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배지 수정 요청 JSON 파트")
public record UpdateBadgeRequest(
        @Schema(description = "배지 이름", example = "첫 정답 배지 수정", requiredMode = Schema.RequiredMode.REQUIRED)
        String badgeName,

        @Schema(description = "배지 설명", example = "첫 문제 정답 달성 배지입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        String description,

        @Schema(description = "배지 지급 기준 누적 포인트", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer requiredPoint,

        @Schema(description = "배지 상태. ACTIVE는 노출 및 신규 지급 가능, INACTIVE는 신규 지급 중단 상태입니다.", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String status
) {
}
