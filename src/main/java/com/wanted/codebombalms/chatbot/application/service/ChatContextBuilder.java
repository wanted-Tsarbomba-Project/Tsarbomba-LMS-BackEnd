package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatContextBuilder {

    private final ChatContextPort chatContextPort;
    private final ChatMessageRepository chatMessageRepository;

    @Value("${chat.max-history-messages}")
    private int maxHistoryMessages;

    public ChatContext build(SendMessageCommand command, ChatRoom chatRoom) {
        ChatContextPort.ProblemSetInfo problemSetInfo = null;
        ChatContextPort.ProblemInfo problemInfo = null;
        ChatContextPort.SubmissionInfo submissionInfo = null;
        ChatContextPort.SessionProgressInfo sessionProgressInfo = null;
        ChatContextPort.DatasetInfo datasetInfo = null;

        if (chatRoom.getProblemSetId() != null) {
            problemSetInfo = chatContextPort.findProblemSet(chatRoom.getProblemSetId());
            sessionProgressInfo = chatContextPort.findSessionProgress(chatRoom.getProblemSetId());
        }

        if (chatRoom.getProblemId() != null) {
            problemInfo = chatContextPort.findProblem(chatRoom.getProblemId());
            submissionInfo = chatContextPort.findLatestSubmission(command.userId(), chatRoom.getProblemId());
            datasetInfo = chatContextPort.findDataset(chatRoom.getProblemId());
        }

        var conversationHistory = chatMessageRepository.findRecentByRoomId(
                command.roomId(), maxHistoryMessages
        );

        return new ChatContext(
                command.userId(),
                command.roomId(),
                command.userMessage(),
                chatRoom.getProblemSetId(),
                chatRoom.getProblemId(),
                problemSetInfo,
                problemInfo,
                submissionInfo,
                sessionProgressInfo,
                datasetInfo,
                conversationHistory
        );
    }
}
