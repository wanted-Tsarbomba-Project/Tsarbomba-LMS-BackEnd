package com.wanted.codebombalms.chatbot.presentation.api;

import com.wanted.codebombalms.chatbot.application.command.CreateChatRoomCommand;
import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;
import com.wanted.codebombalms.chatbot.application.usecase.CreateChatRoomUseCase;
import com.wanted.codebombalms.chatbot.presentation.api.request.ChatRoomCreateRequest;
import com.wanted.codebombalms.chatbot.presentation.api.response.ChatRoomResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final CreateChatRoomUseCase createChatRoomUseCase;

    @Operation(summary = "채팅방 생성", description = "기존 방 있으면 200, 새 방이면 201")
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
            @AuthenticationPrincipal Long userId,
            @RequestBody ChatRoomCreateRequest request
    ) {
        CreateChatRoomCommand command = new CreateChatRoomCommand(userId, request.problemSetId());
        ChatRoomResult result = createChatRoomUseCase.handle(command);

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
}