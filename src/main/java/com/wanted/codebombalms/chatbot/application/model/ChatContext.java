package com.wanted.codebombalms.chatbot.application.model;

import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;

import java.util.List;

public record ChatContext(
        Long userId,
        Long roomId,
        String userMessage,
        ChatContextPort.ProblemSetInfo problemSetInfo,
        List<ChatContextPort.ProblemInfo> problemInfos,
        ChatContextPort.SessionProgressInfo sessionProgressInfo,
        ChatContextPort.DatasetInfo datasetInfo,
        List<ChatMessage> conversationHistory
) {}
