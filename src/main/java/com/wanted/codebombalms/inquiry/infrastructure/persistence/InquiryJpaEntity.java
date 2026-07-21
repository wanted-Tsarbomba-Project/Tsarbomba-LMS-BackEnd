package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@NoArgsConstructor
public class InquiryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long inquiryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InquiryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private InquirySeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain", nullable = false, length = 50)
    private InquiryDomain domain;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "estimated_url", length = 500)
    private String estimatedUrl;

    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    @Column(name = "replied_by")
    private Long repliedBy;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @Column(name = "ai_summary", length = 500)
    private String aiSummary;

    @Column(name = "ai_recommended_action", columnDefinition = "TEXT")
    private String aiRecommendedAction;

    @Column(name = "is_filtered", nullable = false)
    private boolean filtered;

    @Column(name = "reply_visible", nullable = false)
    private boolean replyVisible;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public InquiryJpaEntity(
            Long inquiryId,
            Long userId,
            String title,
            String content,
            InquiryStatus status,
            InquirySeverity severity,
            InquiryDomain domain,
            String sourceUrl,
            String estimatedUrl,
            String adminReply,
            Long repliedBy,
            LocalDateTime repliedAt,
            String aiSummary,
            String aiRecommendedAction,
            boolean filtered,
            boolean replyVisible,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.inquiryId = inquiryId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.severity = severity;
        this.domain = domain;
        this.sourceUrl = sourceUrl;
        this.estimatedUrl = estimatedUrl;
        this.adminReply = adminReply;
        this.repliedBy = repliedBy;
        this.repliedAt = repliedAt;
        this.aiSummary = aiSummary;
        this.aiRecommendedAction = aiRecommendedAction;
        this.filtered = filtered;
        this.replyVisible = replyVisible;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
