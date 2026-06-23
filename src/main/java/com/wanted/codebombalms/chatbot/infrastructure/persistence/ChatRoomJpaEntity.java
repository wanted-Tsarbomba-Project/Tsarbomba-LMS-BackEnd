package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "chat_room",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_chat_room_user_problem",
                columnNames = {"user_id", "problem_set_id", "problem_id"}
        )
)
@Getter
@NoArgsConstructor
public class ChatRoomJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "problem_set_id")
    private Long problemSetId;

    @Column(name = "problem_id")
    private Long problemId;

    @Column(name = "title")
    private String title;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public ChatRoomJpaEntity(Long id, Long userId, Long problemSetId, Long problemId, String title,
                             Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.problemSetId = problemSetId;
        this.problemId = problemId;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
