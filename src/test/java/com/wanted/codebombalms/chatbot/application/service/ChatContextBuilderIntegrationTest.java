package com.wanted.codebombalms.chatbot.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;
import com.wanted.codebombalms.chatbot.domain.model.ChatRoom;
import com.wanted.codebombalms.chatbot.domain.repository.ChatRoomRepository;
import com.wanted.codebombalms.chatbot.infrastructure.client.FastApiChatRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("local")
@EnableAutoConfiguration(exclude = {
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
})
class ChatContextBuilderIntegrationTest {

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ChatContextBuilder chatContextBuilder;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Test
    void 실제_DB로_ChatContext_빌드_결과_확인() throws Exception {
        Long userId = 3L;
        Long roomId = 1L;
        String userMessage = "4번 문제 풀이 도와줘";

        ChatRoom chatRoom = chatRoomRepository.getById(roomId);

        SendMessageCommand command = new SendMessageCommand(userId, roomId, userMessage);
        ChatContext context = chatContextBuilder.build(command, chatRoom);

        ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        System.out.println("========== ChatContext ==========");
        System.out.println("userId: " + context.userId());
        System.out.println("roomId: " + context.roomId());
        System.out.println("userMessage: " + context.userMessage());
        System.out.println("problemSetInfo: " + context.problemSetInfo());
        System.out.println("problemInfos: " + context.problemInfos());
        System.out.println("sessionProgressInfo: " + context.sessionProgressInfo());
        System.out.println("datasetInfo: " + context.datasetInfo());
        System.out.println("conversationHistory 개수: " + context.conversationHistory().size());
        for (ChatMessage msg : context.conversationHistory()) {
            System.out.println("  [" + msg.getRole() + "] " + msg.getContent());
        }

        FastApiChatRequest request = toRequest(context);
        String json = om.writeValueAsString(request);
        System.out.println("========== FastApiChatRequest JSON ==========");
        System.out.println(json);
    }

    private FastApiChatRequest toRequest(ChatContext context) {
        List<FastApiChatRequest.MessageDto> history = context.conversationHistory().stream()
                .map(m -> FastApiChatRequest.MessageDto.builder()
                        .role(m.getRole().name().toLowerCase())
                        .content(m.getContent())
                        .build())
                .collect(Collectors.toList());

        List<FastApiChatRequest.ProblemDto> problems = context.problemInfos().stream()
                .map(p -> FastApiChatRequest.ProblemDto.builder()
                        .title(p.title())
                        .content(p.content())
                        .problemType(p.problemType())
                        .answer(p.answer())
                        .explanation(p.explanation())
                        .submittedAnswer(p.submittedAnswer())
                        .build())
                .collect(Collectors.toList());

        ChatContextPort.ProblemSetInfo ps = context.problemSetInfo();
        FastApiChatRequest.ProblemSetDto problemSetDto = ps == null ? null :
                FastApiChatRequest.ProblemSetDto.builder()
                        .problemSetId(ps.problemSetId())
                        .title(ps.title())
                        .description(ps.description())
                        .build();

        ChatContextPort.SessionProgressInfo sp = context.sessionProgressInfo();
        FastApiChatRequest.SessionProgressDto sessionProgressDto = sp == null ? null :
                FastApiChatRequest.SessionProgressDto.builder()
                        .currentProblemNumber(sp.currentProblemNumber())
                        .build();

        ChatContextPort.DatasetInfo di = context.datasetInfo();
        FastApiChatRequest.DatasetDto datasetDto = di == null ? null :
                FastApiChatRequest.DatasetDto.builder()
                        .metaData(di.metaData())
                        .build();

        return FastApiChatRequest.builder()
                .userMessage(context.userMessage())
                .problemSet(problemSetDto)
                .problems(problems.isEmpty() ? null : problems)
                .sessionProgress(sessionProgressDto)
                .dataset(datasetDto)
                .conversationHistory(history)
                .build();
    }
}
