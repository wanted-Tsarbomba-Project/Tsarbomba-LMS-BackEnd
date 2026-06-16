package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.application.port.ProblemTitlePort;
import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;
import com.wanted.codebombalms.chatbot.application.usecase.ChatRoomQueryUseCase;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService implements ChatRoomQueryUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final ProblemTitlePort problemTitlePort;   // 주입 추가


    @Override
    public List<ChatRoomResult> listRooms(Long userId) {
        return chatRoomRepository.findByUserId(userId)
                .stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Long> findProblemRoomId(Long userId, Long problemSetId, Long problemId) {
        return chatRoomRepository.findByUserIdAndProblem(userId, problemSetId, problemId)
                .map(ChatRoom::getId);
    }


    private ChatRoomResult toResult(ChatRoom chatRoom) {
        return new ChatRoomResult(
                chatRoom.getId(),
                chatRoom.getProblemSetId(),
                chatRoom.getProblemId(),
                chatRoom.getTitle(),
                problemTitlePort.findProblemSetTitleOrNull(chatRoom.getProblemSetId()),
                problemTitlePort.findProblemTitleOrNull(chatRoom.getProblemId()),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt()
        );
    }
}
