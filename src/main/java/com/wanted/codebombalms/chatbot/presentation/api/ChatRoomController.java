package com.wanted.codebombalms.chatbot.presentation.api;

import com.wanted.codebombalms.chatbot.application.command.SendFirstMessageCommand;
import com.wanted.codebombalms.chatbot.presentation.api.request.SendFirstMessageRequest;
import com.wanted.codebombalms.chatbot.application.usecase.ChatRoomCommandUseCase;
import com.wanted.codebombalms.chatbot.application.usecase.ChatRoomQueryUseCase;
import com.wanted.codebombalms.chatbot.presentation.api.response.ChatRoomResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageCommandUseCase;
import com.wanted.codebombalms.chatbot.presentation.api.request.ChatMessageRequest;
import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.stream.Collectors;
import com.wanted.codebombalms.chatbot.presentation.api.request.RenameChatRoomRequest;
import com.wanted.codebombalms.chatbot.presentation.api.response.RenameChatRoomResponse;
import com.wanted.codebombalms.chatbot.presentation.api.response.ChatRoomIdResponse;

@Tag(name = "Chatbot - 채팅방", description = "AI 채팅방 생성/조회/삭제 및 메시지 전송 API")
@PreAuthorize("isAuthenticated()")  // ← 추가
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomCommandUseCase chatRoomCommandUseCase;
    private final ChatRoomQueryUseCase chatRoomQueryUseCase;
    private final ChatMessageCommandUseCase chatMessageCommandUseCase;

    @Operation(
            summary = "첫 메시지 전송 (채팅방 자동 생성, SSE 스트리밍)",
            description = """
                    채팅방을 생성/조회하고 AI 응답을 SSE로 스트리밍합니다.
                    - event: room  → {"roomId": N} (맨 앞 1회)
                    - (event 없음) → {"t": "토큰"} (N회)
                    - event: done  → {"promptTokens","completionTokens","totalTokens"}
                    - event: error → {"code","message"} (스트림 중 에러)
                    진입부 검증 에러(권한 등)는 스트림 시작 전이므로 기존 JSON 에러 응답을 따릅니다."""
    )
    @PostMapping(value = "/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ServerSentEvent<Object>>> sendFirstMessage(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SendFirstMessageRequest request
    ) {
        SendFirstMessageCommand command = new SendFirstMessageCommand(
                userId,
                request.userMessage(),
                request.problemSetId(),
                request.problemId()
        );

        // sendFirst() 호출 시 방 준비(tx)가 동기 실행됨 → 진입부 예외는 여기서 JSON으로 처리됨
        Flux<ServerSentEvent<Object>> body = chatRoomCommandUseCase.sendFirst(command)
                .map(this::toServerSentEvent);

        return ResponseEntity.ok()
                .header("X-Accel-Buffering", "no")
                .body(body);
    }
    @Operation(
            summary = "채팅방 목록 조회",
            description = "로그인한 사용자의 채팅방 목록을 최신순으로 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "목록 조회 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 200,
                                              "code": "CHT-002",
                                              "message": "채팅방 목록을 조회했습니다.",
                                              "data": [
                                                {
                                                  "roomId": 1,
                                                  "problemSetId": 10,
                                                  "problemId": 100,
                                                  "title": "문제 100 채팅방",
                                                  "problemSetTitle": "직원 성과 데이터 기초 분석",
                                                  "problemTitle": "데이터 행 개수 확인",
                                                  "createdAt": "2026-05-28T12:00:00Z",
                                                  "updatedAt": "2026-05-28T12:00:00Z"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "CHT-002 - 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 403,
                                              "code": "CHT-002",
                                              "message": "채팅방에 접근 권한이 없습니다.",
                                              "path": "/api/v1/chat/list"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> listChatRooms(
            @AuthenticationPrincipal Long userId
    ) {
        List<ChatRoomResponse> response = chatRoomQueryUseCase.listRooms(userId)
                .stream()
                .map(ChatRoomResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success(
                        ChatResponseCode.ROOM_RETRIEVED,
                        ChatResponseMessage.ROOM_RETRIEVED,
                        response
                )
        );
    }

    @Operation(
            summary = "메시지 전송 (SSE 스트리밍)",
            description = "유저 메시지를 저장하고 FastAPI를 호출해 AI 응답을 토큰 단위 SSE로 스트리밍합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "CHT-002 - 권한 없음 (스트림 전 JSON)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "CHT-001 - 채팅방 없음 (스트림 전 JSON)")
    })
    @PostMapping(value = "/{roomId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ServerSentEvent<Object>>> sendMessage(
            @Parameter(description = "메시지를 전송할 채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        SendMessageCommand command = new SendMessageCommand(userId, roomId, request.userMessage());

        Flux<ServerSentEvent<Object>> body = chatMessageCommandUseCase.send(command)
                .map(this::toServerSentEvent);

        return ResponseEntity.ok()
                .header("X-Accel-Buffering", "no")
                .body(body);
    }

    @Operation(
            summary = "채팅방 제목 수정",
            description = "채팅방 제목을 변경합니다. 본인 채팅방만 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "제목 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "CHT-002 - 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "CHT-001 - 채팅방 없음")
    })
    @PatchMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RenameChatRoomResponse>> renameChatRoom(
            @Parameter(description = "제목을 수정할 채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RenameChatRoomRequest request
    ) {
        RenameChatRoomResponse response = RenameChatRoomResponse.from(
                chatRoomCommandUseCase.rename(roomId, userId, request.title())
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        ChatResponseCode.ROOM_RENAMED,
                        ChatResponseMessage.ROOM_RENAMED,
                        response
                )
        );
    }

    @Operation(
            summary = "문제 채팅방 단건 조회",
            description = "문제 풀이 진입 시 해당 문제의 기존 채팅방을 조회합니다. 없으면 204."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "기존 방 존재 → roomId 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", description = "해당 문제의 방 없음")
    })
    @GetMapping("/room")
    public ResponseEntity<ApiResponse<ChatRoomIdResponse>> findProblemRoom(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long problemSetId,
            @RequestParam Long problemId
    ) {
        return chatRoomQueryUseCase.findProblemRoomId(userId, problemSetId, problemId)
                .map(roomId -> ResponseEntity.ok(
                        ApiResponse.success(
                                ChatResponseCode.ROOM_RETRIEVED,
                                ChatResponseMessage.ROOM_FOUND,
                                new ChatRoomIdResponse(roomId)
                        )
                ))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }


    @Operation(
            summary = "채팅방 삭제",
            description = "채팅방과 포함된 모든 메시지를 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "채팅방 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "CHT-002 - 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 403,
                                              "code": "CHT-002",
                                              "message": "채팅방에 접근 권한이 없습니다.",
                                              "path": "/api/v1/chat/1"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "CHT-001 - 채팅방 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "채팅방 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 404,
                                              "code": "CHT-001",
                                              "message": "채팅방을 찾을 수 없습니다.",
                                              "path": "/api/v1/chat/9999"
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteChatRoom(
            @Parameter(description = "삭제할 채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId
    ) {
        chatRoomCommandUseCase.delete(roomId, userId);
        return ResponseEntity.noContent().build();
    }

    /** 도메인 청크를 브라우저 SSE 프레임으로 변환 (ADR-0005 이벤트 규약). */
    private ServerSentEvent<Object> toServerSentEvent(AiChatStreamChunk chunk) {
        if (chunk instanceof AiChatStreamChunk.Room r) {
            return ServerSentEvent.builder((Object) new SseRoom(r.roomId())).event("room").build();
        }
        if (chunk instanceof AiChatStreamChunk.Token t) {
            return ServerSentEvent.builder((Object) new SseToken(t.text())).build(); // event 이름 없음
        }
        if (chunk instanceof AiChatStreamChunk.Done d) {
            return ServerSentEvent.builder((Object) d.usage()).event("done").build();
        }
        if (chunk instanceof AiChatStreamChunk.Error e) {
            return ServerSentEvent.builder((Object) new SseError(e.code(), e.message())).event("error").build();
        }
        throw new IllegalStateException("알 수 없는 스트림 청크: " + chunk);
    }

    private record SseRoom(Long roomId) {}
    private record SseToken(String t) {}
    private record SseError(String code, String message) {}
}
