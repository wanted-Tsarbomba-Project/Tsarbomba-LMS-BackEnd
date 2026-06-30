package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatMessageRepository;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 채팅 메시지 전송의 트랜잭션 경계 두 개를 담는다.
 * 스트리밍 오케스트레이션(ChatMessageCommandService)과 분리해야
 * @Transactional 프록시가 실제로 적용된다(self-invocation 회피).
 */
@Service
@RequiredArgsConstructor
public class ChatMessageTransactionService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatContextBuilder chatContextBuilder;

    /** 스트림 전 동기 구간: 방 검증 + USER 메시지 저장 + 컨텍스트 조립(타 BC 포트 호출 포함). */
    @Transactional
    public ChatContext prepare(SendMessageCommand command) {
        ChatRoom chatRoom = chatRoomRepository.getById(command.roomId());
        chatRoom.verifyOwner(command.userId());

        chatMessageRepository.save(
                ChatMessage.createUserMessage(command.roomId(), command.userMessage())
        );

        return chatContextBuilder.build(command, chatRoom);
    }

    /** 스트림 정상 완료 시에만 호출: AI 답변(토큰 사용량 포함) 저장 + 방 타임스탬프 갱신. */
    @Transactional
    public void saveAiAnswer(Long roomId, String answer, AiChatStreamChunk.TokenUsage usage) {
        chatMessageRepository.save(ChatMessage.createAiMessage(
                roomId,
                answer,
                usage.promptTokens(),
                usage.completionTokens(),
                usage.totalTokens()
        ));

        ChatRoom chatRoom = chatRoomRepository.getById(roomId);
        chatRoom.updateTimestamp(Instant.now());
        chatRoomRepository.save(chatRoom);
    }
}
