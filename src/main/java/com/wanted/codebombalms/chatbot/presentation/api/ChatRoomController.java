package com.wanted.codebombalms.chatbot.presentation.api;

import com.wanted.codebombalms.chatbot.application.command.CreateChatRoomCommand;
import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;
import com.wanted.codebombalms.chatbot.application.usecase.ChatRoomCommandUseCase;
import com.wanted.codebombalms.chatbot.application.usecase.ChatRoomQueryUseCase;
import com.wanted.codebombalms.chatbot.presentation.api.request.ChatRoomCreateRequest;
import com.wanted.codebombalms.chatbot.presentation.api.response.ChatRoomResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
// import 추가
import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageCommandUseCase;
import com.wanted.codebombalms.chatbot.presentation.api.request.ChatMessageRequest;
import com.wanted.codebombalms.chatbot.presentation.api.response.AiChatResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomCommandUseCase chatRoomCommandUseCase;
    private final ChatRoomQueryUseCase chatRoomQueryUseCase;
    private final ChatMessageCommandUseCase chatMessageCommandUseCase;

    @Operation(summary = "채팅방 생성", description = "기존 방 있으면 200, 새 방이면 201")
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
            @AuthenticationPrincipal Long userId,
            @RequestBody ChatRoomCreateRequest request
    ) {
        CreateChatRoomCommand command = new CreateChatRoomCommand(userId, request.problemSetId());
        ChatRoomResult result = chatRoomCommandUseCase.create(command);

        ChatRoomResponse response = ChatRoomResponse.from(result);

        boolean isNew = result.updatedAt() == null;

        if (isNew) {
            return ResponseEntity.status(201).body(
                    ApiResponse.created(
                            ChatResponseCode.ROOM_CREATED,
                            ChatResponseMessage.ROOM_CREATED,
                            response
                    )
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        ChatResponseCode.ROOM_RETRIEVED,
                        ChatResponseMessage.ROOM_RETRIEVED,
                        response
                )
        );
    }

    @Operation(summary = "채팅방 목록 조회", description = "userId 기준 채팅방 목록 최신순 반환")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> listChatRooms(
            @AuthenticationPrincipal Long userId
    ) {
        List<ChatRoomResponse> response = chatRoomQueryUseCase.listRooms(userId)
                .stream()
                .map(ChatRoomResponse::from)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success(
                        ChatResponseCode.ROOM_RETRIEVED,
                        ChatResponseMessage.ROOM_RETRIEVED,
                        response
                )
        );
    }


    @Operation(summary = "메시지 전송", description = "유저 메시지 저장 + FastAPI 호출 + AI 응답 반환")
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<AiChatResponse>> sendMessage(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId,
            @RequestBody ChatMessageRequest request
    ) {
        SendMessageCommand command = new SendMessageCommand(
                userId,
                roomId,
                request.problemId(),
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

}