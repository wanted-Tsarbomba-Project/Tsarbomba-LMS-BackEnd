package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import com.wanted.codebombalms.chatbot.domain.model.FeedbackRating;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "chat_message_feedback",
        uniqueConstraints = @UniqueConstraint(name = "uq_feedback_message", columnNames = "message_id")
)
@Getter
@NoArgsConstructor
public class MessageFeedbackJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false, length = 4)
    private FeedbackRating rating;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MessageFeedbackJpaEntity(
            Long id,
            Long messageId,
            Long userId,
            FeedbackRating rating,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.messageId = messageId;
        this.userId = userId;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
