package com.wanted.codebombalms.ranking.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.ranking.application.usecase.RankingQueryUseCase;
import com.wanted.codebombalms.ranking.presentation.response.MyRankingResponse;
import com.wanted.codebombalms.ranking.presentation.response.RankingListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "포인트 랭킹", description = "학생들의 누적 포인트와 주간 포인트 기준 랭킹 조회 API")
@RestController
@RequestMapping("/api/v1/rankings/points")
@RequiredArgsConstructor
public class RankingController {

    private final RankingQueryUseCase rankingQueryUseCase;

    @Operation(
            summary = "전체 포인트 랭킹 조회",
            description = """
                    학생들의 누적 포인트를 기준으로 전체 랭킹을 조회합니다.
                    user_point.total_point 값을 기준으로 높은 포인트 순서대로 정렬합니다.
                    동점자는 같은 순위를 가집니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전체 포인트 랭킹 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<RankingListResponse>> getTotalPointRankings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        var result = rankingQueryUseCase.getTotalPointRankings(page, size);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "전체 포인트 랭킹 조회에 성공했습니다.",
                RankingListResponse.from(result)
        ));
    }

    @Operation(
            summary = "주간 포인트 랭킹 조회",
            description = """
                    최근 7일 동안 지급된 포인트를 기준으로 주간 랭킹을 조회합니다.
                    point_history.created_at 기준으로 최근 7일 데이터를 합산합니다.
                    동점자는 같은 순위를 가집니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주간 포인트 랭킹 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<RankingListResponse>> getWeeklyPointRankings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = rankingQueryUseCase.getWeeklyPointRankings(page, size);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "주간 포인트 랭킹 조회에 성공했습니다.",
                RankingListResponse.from(result)
        ));
    }

    @Operation(
            summary = "내 포인트 랭킹 조회",
            description = """
                    로그인한 사용자의 전체 포인트 랭킹을 조회합니다.
                    userId는 요청 파라미터로 받지 않고 인증 토큰에서 가져옵니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 포인트 랭킹 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RNK-001 - 랭킹 정보를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyRankingResponse>> getMyPointRanking(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId
    ) {
        var result = rankingQueryUseCase.getMyPointRanking(userId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "내 포인트 랭킹 조회에 성공했습니다.",
                MyRankingResponse.from(result)
        ));
    }
}
