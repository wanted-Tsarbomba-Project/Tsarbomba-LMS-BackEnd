package com.wanted.codebombalms.chatbot.presentation.api;

import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageQueryUseCase;
import com.wanted.codebombalms.chatbot.presentation.api.response.ChatMessageResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageQueryUseCase chatMessageQueryUseCase;

    @Operation(summary = "채팅 내역 조회", description = "roomId 기준 메시지 목록 조회, 소유권 검증 포함")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> listMessages(
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