package com.wanted.codebombalms.badge.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    private Long badgeId;
    private String badgeName;
    private String description;
    private Integer requiredPoint;

    private String originalFileName;
    private String objectName;
    private String contentType;
    private Long fileSize;

    private BadgeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Badge create(
            String badgeName,
            String description,
            Integer requiredPoint,
            String originalFileName,
            String objectName,
            String contentType,
            Long fileSize
    ) {
        Badge badge = new Badge();
        badge.badgeName = badgeName;
        badge.description = description;
        badge.requiredPoint = requiredPoint;
        badge.originalFileName = originalFileName;
        badge.objectName = objectName;
        badge.contentType = contentType;
        badge.fileSize = fileSize;
        badge.status = BadgeStatus.ACTIVE;
        return badge;
    }

    public static Badge restore(
            Long badgeId,
            String badgeName,
            String description,
            Integer requiredPoint,
            String originalFileName,
            String objectName,
            String contentType,
            Long fileSize,
            BadgeStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        return new Badge(
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

    public void update(
            String badgeName,
            String description,
            Integer requiredPoint,
            BadgeStatus status
    ) {
        this.badgeName = badgeName;
        this.description = description;
        this.requiredPoint = requiredPoint;
        this.status = status;
    }

    public void replaceImage(
            String originalFileName,
            String objectName,
            String contentType,
            Long fileSize
    ) {
        this.originalFileName = originalFileName;
        this.objectName = objectName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public void delete() {
        this.status = BadgeStatus.INACTIVE;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean canBeGranted() {
        return status == BadgeStatus.ACTIVE && deletedAt == null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
