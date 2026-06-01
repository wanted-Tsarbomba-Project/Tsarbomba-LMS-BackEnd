package com.wanted.codebombalms.chatbot.presentation.api;

import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageQueryUseCase;
import com.wanted.codebombalms.chatbot.presentation.api.response.ChatMessageResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Chatbot - 채팅 메시지", description = "채팅 메시지 내역 조회 API")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")  // ← 추가
public class ChatMessageController {

    private final ChatMessageQueryUseCase chatMessageQueryUseCase;

    @Operation(
            summary = "채팅 내역 조회",
            description = "roomId 기준으로 해당 채팅방의 메시지 목록을 시간순으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "채팅 내역 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "내역 조회 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 200,
                                              "code": "CHT-004",
                                              "message": "채팅 내역을 조회했습니다.",
                                              "data": [
                                                {
                                                  "messageId": 1,
                                                  "role": "USER",
                                                  "content": "힌트 주세요",
                                                  "createdAt": "2026-05-28T12:00:00Z"
                                                },
                                                {
                                                  "messageId": 2,
                                                  "role": "ASSISTANT",
                                                  "content": "배열의 합을 누적합으로 접근해보세요.",
                                                  "createdAt": "2026-05-28T12:00:01Z"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "CHT-002 - 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 401,
                                              "code": "CHT-002",
                                              "message": "채팅방에 접근 권한이 없습니다.",
                                              "path": "/api/v1/chat/1/messages"
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
                                              "path": "/api/v1/chat/9999/messages"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> listMessages(
            @Parameter(description = "조회할 채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId
    ) {
        List<ChatMessageResponse> response = chatMessageQueryUseCase.listMessages(roomId, userId)
                .stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success(
                        ChatResponseCode.MESSAGES_RETRIEVED,
                        ChatResponseMessage.MESSAGES_RETRIEVED,
                        response
                )
        );
    }
}
