package com.wanted.codebombalms.chatbot.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class ChatRoomTest {

    @Test
    void 본인이면_verifyOwner_통과() {
        ChatRoom room = ChatRoom.create(1L, 10L);
        assertThatNoException().isThrownBy(() -> room.verifyOwner(1L));
    }

    @Test
    void 다른_userId면_verifyOwner_ForbiddenException() {
        ChatRoom room = ChatRoom.create(1L, 10L);
        assertThatThrownBy(() -> room.verifyOwner(2L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateTimestamp_호출시_updatedAt_변경() {
        ChatRoom room = ChatRoom.restore(1L, 1L, 10L, Instant.now(), null);
        Instant now = Instant.now();

        room.updateTimestamp(now);

        assertThat(room.getUpdatedAt()).isEqualTo(now);
    }
}