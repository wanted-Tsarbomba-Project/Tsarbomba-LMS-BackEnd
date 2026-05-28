package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.application.port.ChatContextPort.*;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatContextBuilderTest {

    @InjectMocks
    private ChatContextBuilder chatContextBuilder;

    @Mock
    private ChatContextPort chatContextPort;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    private void setMaxHistoryMessages(int value) throws Exception {
        Field field = ChatContextBuilder.class.getDeclaredField("maxHistoryMessages");
        field.setAccessible(true);
        field.setInt(chatContextBuilder, value);
    }

    // ── 케이스 1: problemSetId + problemId 둘 다 있음 → 풀 컨텍스트 ──

    @Test
    void 풀_컨텍스트_problemSetId_problemId_모두_있으면_전부_채워짐() throws Exception {
        setMaxHistoryMessages(20);

        SendMessageCommand command = new SendMessageCommand(1L, 100L, "질문입니다");
        ChatRoom chatRoom = ChatRoom.restore(100L, 1L, 10L, 50L, "제목", Instant.now(), Instant.now());

        ProblemSetInfo psInfo = new ProblemSetInfo(10L, "문제집", "설명");
        List<ProblemInfo> problems = List.of(
                new ProblemInfo("문제1", "내용1", "TEXT", "정답1", "해설1", "제출답1")
        );
        SessionProgressInfo spInfo = new SessionProgressInfo(3);
        DatasetInfo dsInfo = new DatasetInfo("meta");
        List<ChatMessage> history = List.of(
                ChatMessage.restore(1L, 100L, com.wanted.codebombalms.chatbot.domain.model.MessageRole.USER, "이전 질문", Instant.now())
        );

        when(chatContextPort.findProblemSet(10L)).thenReturn(psInfo);
        when(chatContextPort.findProblems(10L, 1L)).thenReturn(problems);
        when(chatContextPort.findSessionProgress(50L)).thenReturn(spInfo);
        when(chatContextPort.findDataset(10L)).thenReturn(dsInfo);
        when(chatMessageRepository.findRecentByRoomId(100L, 20)).thenReturn(history);

        ChatContext result = chatContextBuilder.build(command, chatRoom);

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.roomId()).isEqualTo(100L);
        assertThat(result.userMessage()).isEqualTo("질문입니다");
        assertThat(result.problemSetInfo()).isEqualTo(psInfo);
        assertThat(result.problemInfos()).hasSize(1);
        assertThat(result.sessionProgressInfo()).isEqualTo(spInfo);
        assertThat(result.datasetInfo()).isEqualTo(dsInfo);
        assertThat(result.conversationHistory()).hasSize(1);
    }

    // ── 케이스 2: problemSetId 있고 problemId null → sessionProgress null ──

    @Test
    void problemId_null이면_sessionProgress_null() throws Exception {
        setMaxHistoryMessages(20);

        SendMessageCommand command = new SendMessageCommand(1L, 100L, "질문");
        ChatRoom chatRoom = ChatRoom.restore(100L, 1L, 10L, null, "제목", Instant.now(), Instant.now());

        when(chatContextPort.findProblemSet(10L)).thenReturn(new ProblemSetInfo(10L, "문제집", "설명"));
        when(chatContextPort.findProblems(10L, 1L)).thenReturn(List.of());
        when(chatContextPort.findDataset(10L)).thenReturn(null);
        when(chatMessageRepository.findRecentByRoomId(100L, 20)).thenReturn(List.of());

        ChatContext result = chatContextBuilder.build(command, chatRoom);

        assertThat(result.problemSetInfo()).isNotNull();
        assertThat(result.sessionProgressInfo()).isNull();
    }

    // ── 케이스 3: problemSetId null (자유 질문 모드) → 전부 null/empty ──

    @Test
    void 자유질문모드_problemSetId_null이면_컨텍스트_비어있음() throws Exception {
        setMaxHistoryMessages(20);

        SendMessageCommand command = new SendMessageCommand(1L, 100L, "자유 질문");
        ChatRoom chatRoom = ChatRoom.restore(100L, 1L, null, null, "자유채팅", Instant.now(), Instant.now());

        when(chatMessageRepository.findRecentByRoomId(100L, 20)).thenReturn(List.of());

        ChatContext result = chatContextBuilder.build(command, chatRoom);

        assertThat(result.problemSetInfo()).isNull();
        assertThat(result.problemInfos()).isEmpty();
        assertThat(result.sessionProgressInfo()).isNull();
        assertThat(result.datasetInfo()).isNull();
        assertThat(result.userMessage()).isEqualTo("자유 질문");

        verify(chatContextPort, never()).findProblemSet(any());
        verify(chatContextPort, never()).findProblems(any(), any());
    }
}
