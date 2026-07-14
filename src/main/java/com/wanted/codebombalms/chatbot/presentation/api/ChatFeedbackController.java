package com.wanted.codebombalms.chatbot.presentation.api;

import com.wanted.codebombalms.chatbot.application.command.SaveFeedbackCommand;
import com.wanted.codebombalms.chatbot.application.usecase.MessageFeedbackCommandUseCase;
import com.wanted.codebombalms.chatbot.presentation.api.request.MessageFeedbackRequest;
import com.wanted.codebombalms.chatbot.presentation.api.response.MessageFeedbackResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Chatbot - 메시지 피드백", description = "AI 응답 품질 피드백(👍/👎) API")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatFeedbackController {

    private final MessageFeedbackCommandUseCase messageFeedbackCommandUseCase;

    @Operation(
            summary = "메시지 평가 설정/전환 (👍/👎)",
            description = "AI 응답 메시지에 UP/DOWN 평가를 남깁니다. 이미 있으면 값을 전환합니다(멱등)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "평가 저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "CHT-007 - AI 응답 메시지가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "CHT-002 - 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "CHT-006 - 메시지 없음")
    })
    @PutMapping("/messages/{messageId}/feedback")
    public ResponseEntity<ApiResponse<MessageFeedbackResponse>> saveFeedback(
            @Parameter(description = "평가할 AI 메시지 ID", example = "2")
            @PathVariable Long messageId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody MessageFeedbackRequest request
    ) {
        MessageFeedbackResponse response = MessageFeedbackResponse.from(
                messageFeedbackCommandUseCase.save(
                        new SaveFeedbackCommand(userId, messageId, request.rating())
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        ChatResponseCode.FEEDBACK_SAVED,
                        ChatResponseMessage.FEEDBACK_SAVED,
                        response
                )
        );
    }

    @Operation(
            summary = "메시지 평가 취소",
            description = "메시지에 남긴 평가를 삭제합니다. 평가가 없어도 204로 응답합니다(멱등)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", description = "평가 취소 성공 (없어도 no-op)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "CHT-002 - 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "CHT-006 - 메시지 없음")
    })
    @DeleteMapping("/messages/{messageId}/feedback")
    public ResponseEntity<Void> deleteFeedback(
            @Parameter(description = "평가를 취소할 메시지 ID", example = "2")
            @PathVariable Long messageId,
            @AuthenticationPrincipal Long userId
    ) {
        messageFeedbackCommandUseCase.delete(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}
