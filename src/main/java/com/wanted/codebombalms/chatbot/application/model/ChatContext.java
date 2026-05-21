package com.wanted.codebombalms.chatbot.application.model;

import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;

import java.util.List;

public record ChatContext(
        Long userId,
        Long roomId,
        String userMessage,
        Long problemSetId,
        Long problemId,
        ChatContextPort.ProblemSetInfo problemSetInfo,
        ChatContextPort.ProblemInfo problemInfo,
        ChatContextPort.SubmissionInfo submissionInfo,
        ChatContextPort.SessionProgressInfo sessionProgressInfo,
        ChatContextPort.DatasetInfo datasetInfo,
        List<ChatMessage> conversationHistory
) {}