package com.wanted.codebombalms.badge.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배지 등록 요청 JSON 파트")
public record CreateBadgeRequest(
        @Schema(description = "배지 이름", example = "첫 정답 배지", requiredMode = Schema.RequiredMode.REQUIRED)
        String badgeName,

        @Schema(description = "배지 설명", example = "첫 문제를 맞힌 사용자에게 지급되는 배지입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        String description,

        @Schema(description = "배지 지급 기준 누적 포인트", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer requiredPoint
) {
}
