package com.wanted.codebombalms.chatbot.application.usecase;

import com.wanted.codebombalms.chatbot.application.command.SendFirstMessageCommand;
import com.wanted.codebombalms.chatbot.application.result.SendFirstMessageResult;
import com.wanted.codebombalms.chatbot.application.result.RenameChatRoomResult;

public interface ChatRoomCommandUseCase {

    SendFirstMessageResult sendFirst(SendFirstMessageCommand command);

    void delete(Long roomId, Long userId);

    RenameChatRoomResult rename(Long roomId, Long userId, String title);
}
