package com.wanted.codebombalms.serviceevent.presentation.api;

import com.wanted.codebombalms.serviceevent.infrastructure.client.OpsChatFastApiClient;
import com.wanted.codebombalms.serviceevent.presentation.api.request.OpsChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

/**
 * 관리자 운영 Q&A 챗봇 API — ADMIN·MASTER 전용. 권한 설계는 SecuritySummaryController 와 동일.
 *
 * FastAPI(/ops-chat)로 릴레이하고 SSE 를 그대로 흘린다. 관리자 인증이 여기서 끝나므로
 * FastAPI 쪽은 내부망 전용 무인증 (SG 로 Spring 박스만 허용 전제).
 */
@RestController
@RequestMapping("/api/v1/admin/security/ops-chat")
@PreAuthorize("hasAnyRole('ADMIN','MASTER')")
@RequiredArgsConstructor
public class OpsChatController {

    private final OpsChatFastApiClient opsChatFastApiClient;

    @Operation(
            summary = "운영 Q&A 챗봇",
            description = """
                    관리자 질문을 FastAPI 운영 챗봇으로 릴레이하고 SSE 로 스트리밍합니다.
                    LLM 이 function calling 으로 service_event/ops_briefing 을 읽기 전용 조회합니다.
                    - data(이벤트명 없음): {"t": "토큰"}
                    - event: status → {"tool", "message"} (도구 실행 안내)
                    - event: done → 토큰 사용량 (누적)
                    - event: error → {"code": "OPS-001", "message"}
                    진입부 검증 에러(권한 등)는 스트림 시작 전이므로 기존 JSON 에러 응답을 따릅니다.""")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SSE 스트림 시작"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "AUT-015: 권한 없음")
    })
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ServerSentEvent<String>>> chat(@Valid @RequestBody OpsChatRequest request) {
        // SSE 스트림 콜백은 별도 reactor 스레드라 MDC(traceId)가 전파되지 않는다.
        // 호출 스레드인 여기서 캡처해 넘긴다 (학생 챗봇 컨트롤러와 동일 컨벤션).
        String traceId = MDC.get("traceId");

        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_EVENT_STREAM, StandardCharsets.UTF_8))
                .header("X-Accel-Buffering", "no")
                .body(opsChatFastApiClient.stream(request, traceId));
    }
}
