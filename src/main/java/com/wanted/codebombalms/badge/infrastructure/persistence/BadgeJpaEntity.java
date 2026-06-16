package com.wanted.codebombalms.badge.infrastructure.persistence;

import com.wanted.codebombalms.badge.domain.model.Badge;
import com.wanted.codebombalms.badge.domain.model.BadgeStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "badge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BadgeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long badgeId;

    @Column(name = "badge_name", nullable = false)
    private String badgeName;

    @Column(name = "description")
    private String description;

    @Column(name = "required_point", nullable = false)
    private Integer requiredPoint;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "object_name")
    private String objectName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BadgeStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static BadgeJpaEntity from(Badge badge) {
        BadgeJpaEntity entity = new BadgeJpaEntity();
        entity.apply(badge);
        return entity;
    }

    public void apply(Badge badge) {
        this.badgeId = badge.getBadgeId();
        this.badgeName = badge.getBadgeName();
        this.description = badge.getDescription();
        this.requiredPoint = badge.getRequiredPoint();
        this.originalFileName = badge.getOriginalFileName();
        this.objectName = badge.getObjectName();
        this.contentType = badge.getContentType();
        this.fileSize = badge.getFileSize();
        this.status = badge.getStatus();
        this.deletedAt = badge.getDeletedAt();
    }

    public Badge toDomain() {
        return Badge.restore(
                badgeId,
                badgeName,
                description,
                requiredPoint,
                originalFileName,
                objectName,
                contentType,
                fileSize,
                status,
                createdAt,
                updatedAt,
                deletedAt
        );
    }
}
