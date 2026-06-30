package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient;
import com.wanted.codebombalms.chatbot.application.port.RecordTokenUsagePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

/**
 * SSE 스트림이 ERR_INCOMPLETE_CHUNKED_ENCODING 으로 끊기는 버그의 회귀 테스트.
 *
 * <p>HTTP 의 incomplete-chunked == Reactor 시임에서 send() Flux 가 onComplete 가 아니라
 * onError 로 종료되는 것(컨트롤러가 SSE 로 어댑트 → 종료 청크 못 보냄 → abort).
 * 따라서 "스트림 중/종단 에러가 나도 종단 프레임을 내고 정상 complete 되는가" 를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageCommandService 스트림 종료 처리")
class ChatMessageCommandServiceTest {

    @Mock
    private AiChatClient aiChatClient;
    @Mock
    private ChatMessageTransactionService txService;
    @Mock
    private RecordTokenUsagePort recordTokenUsagePort;
    @InjectMocks
    private ChatMessageCommandService service;

    private final SendMessageCommand command = new SendMessageCommand(1L, 10L, "안녕");

    @Test
    @DisplayName("AI 답변 저장이 실패해도 스트림은 onError 로 끊기지 않고 done 으로 정상 완료된다")
    void 저장실패_정상종료() {
        given(txService.prepare(any())).willReturn(null);
        given(aiChatClient.stream(any())).willReturn(Flux.just(
                new AiChatStreamChunk.Token("안"),
                new AiChatStreamChunk.Token("녕"),
                new AiChatStreamChunk.Done(new AiChatStreamChunk.TokenUsage(10, 2, 12))
        ));
        willThrow(new RuntimeException("DB down"))
                .given(txService).saveAiAnswer(anyLong(), anyString(), any());

        List<AiChatStreamChunk> chunks = service.send(command)
                .collectList()
                .block(Duration.ofSeconds(5));

        assertThat(chunks).isNotNull();
        assertThat(chunks).last().isInstanceOf(AiChatStreamChunk.Done.class);
    }

    @Test
    @DisplayName("스트림 중간 에러 신호도 error 프레임으로 변환되어 정상 완료된다")
    void 중간에러_정상종료() {
        given(txService.prepare(any())).willReturn(null);
        given(aiChatClient.stream(any())).willReturn(Flux.concat(
                Flux.just(new AiChatStreamChunk.Token("부분")),
                Flux.error(new RuntimeException("stream broke"))
        ));

        List<AiChatStreamChunk> chunks = service.send(command)
                .collectList()
                .block(Duration.ofSeconds(5));

        assertThat(chunks).isNotNull();
        assertThat(chunks).last().isInstanceOf(AiChatStreamChunk.Error.class);
    }
}
