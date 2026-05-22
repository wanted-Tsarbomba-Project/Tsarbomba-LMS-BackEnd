package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import com.wanted.codebombalms.chatbot.domain.model.MessageRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor
public class ChatMessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MessageRole role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ChatMessageJpaEntity(Long id, Long roomId, MessageRole role, String content, Instant createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}