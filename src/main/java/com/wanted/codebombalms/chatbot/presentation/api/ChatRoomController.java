package com.wanted.codebombalms.chatbot.presentation.api;

import com.wanted.codebombalms.chatbot.application.command.SendFirstMessageCommand;
import com.wanted.codebombalms.chatbot.presentation.api.request.SendFirstMessageRequest;
import com.wanted.codebombalms.chatbot.presentation.api.response.SendFirstMessageResponse;
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
import com.wanted.codebombalms.chatbot.presentation.api.response.AiChatResponse;
import java.util.List;
import java.util.stream.Collectors;

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
            summary = "첫 메시지 전송 (채팅방 자동 생성)",
            description = "채팅방을 생성하고 첫 메시지를 전송합니다. 응답의 roomId로 이후 메시지를 전송합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "채팅방 생성 + 메시지 전송 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                        {
                                          "timestamp": "2026-05-29T12:00:00",
                                          "status": 201,
                                          "code": "CHAT-FIRST-MESSAGE-SENT",
                                          "message": "채팅방이 생성되고 메시지가 전송되었습니다.",
                                          "data": {
                                            "roomId": 42,
                                            "answer": "힌트: 배열의 합을 구할 때는 누적합을 활용해보세요."
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "502",
                    description = "CHT-003 - AI 응답 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "AI 응답 실패",
                                    value = """
                                        {
                                          "timestamp": "2026-05-29T12:00:00",
                                          "status": 502,
                                          "code": "CHT-003",
                                          "message": "AI 응답 생성에 실패했습니다.",
                                          "path": "/api/v1/chat/messages"
                                        }
                                        """
                            )
                    )
            )
    })
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<SendFirstMessageResponse>> sendFirstMessage(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SendFirstMessageRequest request
    ) {
        SendFirstMessageCommand command = new SendFirstMessageCommand(
                userId,
                request.userMessage(),
                request.problemSetId(),
                request.problemId()
        );

        SendFirstMessageResponse response = SendFirstMessageResponse.from(
                chatRoomCommandUseCase.sendFirst(command)
        );

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        ChatResponseCode.FIRST_MESSAGE_SENT,
                        ChatResponseMessage.FIRST_MESSAGE_SENT,
                        response
                )
        );
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
            summary = "메시지 전송",
            description = "유저 메시지를 저장하고 FastAPI를 호출해 AI 응답을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "메시지 전송 및 AI 응답 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "AI 응답 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 200,
                                              "code": "CHT-003",
                                              "message": "메시지가 전송되었습니다.",
                                              "data": {
                                                "answer": "힌트: 배열의 합을 구할 때는 누적합을 활용해보세요."
                                              }
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "CHT-003 - AI 응답 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "AI 응답 실패",
                                    value = """
                                            {
                                              "timestamp": "2026-05-28T12:00:00",
                                              "status": 500,
                                              "code": "CHT-003",
                                              "message": "AI 응답에 실패했습니다.",
                                              "path": "/api/v1/chat/1/messages"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<AiChatResponse>> sendMessage(
            @Parameter(description = "메시지를 전송할 채팅방 ID", example = "1")
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        SendMessageCommand command = new SendMessageCommand(
                userId,
                roomId,
                request.userMessage()
        );

        AiChatResponse response = AiChatResponse.from(chatMessageCommandUseCase.send(command));

        return ResponseEntity.ok(
                ApiResponse.success(
                        ChatResponseCode.MESSAGE_SENT,
                        ChatResponseMessage.MESSAGE_SENT,
                        response
                )
        );
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
}
