package com.wanted.codebombalms.badge.application.service;

import com.wanted.codebombalms.badge.application.command.CreateBadgeCommand;
import com.wanted.codebombalms.badge.application.command.UpdateBadgeCommand;
import com.wanted.codebombalms.badge.application.port.BadgeImageStoragePort;
import com.wanted.codebombalms.badge.application.port.BadgePersistencePort;
import com.wanted.codebombalms.badge.application.port.UserBadgePersistencePort;
import com.wanted.codebombalms.badge.application.query.BadgeResult;
import com.wanted.codebombalms.badge.application.usecase.AdminBadgeUseCase;
import com.wanted.codebombalms.badge.domain.model.Badge;
import com.wanted.codebombalms.badge.domain.model.BadgeStatus;
import com.wanted.codebombalms.badge.exception.BadgeErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminBadgeService implements AdminBadgeUseCase {

    private final BadgePersistencePort badgePersistencePort;
    private final BadgeImageStoragePort badgeImageStoragePort;
    private final UserBadgePersistencePort userBadgePersistencePort;

    @Override
    @Transactional(readOnly = true)
    public List<BadgeResult> getBadges() {
        return badgePersistencePort.findAllNotDeleted()
                .stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BadgeResult getBadge(Long badgeId) {
        Badge badge = getNotDeletedBadge(badgeId);
        return toResult(badge);
    }

    @Override
    public BadgeResult createBadge(CreateBadgeCommand command) {
        validateCreateCommand(command);

        if (badgePersistencePort.existsNotDeletedByName(command.badgeName())) {
            throw new ConflictException(
                    BadgeErrorCode.BADGE_NAME_ALREADY_EXISTS
            );
        }

        var storedImage = badgeImageStoragePort.upload(
                command.originalFileName(),
                command.contentType(),
                command.fileSize(),
                command.imageBytes()
        );

        registerImageRollbackCompensation(storedImage.objectName());

        Badge badge = Badge.create(
                command.badgeName(),
                command.description(),
                command.requiredPoint(),
                storedImage.originalFileName(),
                storedImage.objectName(),
                storedImage.contentType(),
                storedImage.fileSize()
        );

        Badge savedBadge = badgePersistencePort.save(badge);

        return toResult(savedBadge);
    }

    private void validateCreateCommand(CreateBadgeCommand command) {
        if (command == null
                || command.badgeName() == null
                || command.badgeName().isBlank()
                || command.requiredPoint() == null
                || command.requiredPoint() < 0) {
            throw new ValidationException(
                    BadgeErrorCode.BADGE_INVALID_INPUT
            );
        }
    }

    private void registerImageRollbackCompensation(String objectName) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_ROLLED_BACK) {
                            return;
                        }

                        try {
                            badgeImageStoragePort.delete(objectName);
                        } catch (Exception e) {
                            log.error(
                                    "뱃지 등록 롤백 후 GCS 이미지 삭제에 실패했습니다. objectName={}",
                                    objectName,
                                    e
                            );
                        }
                    }
                }
        );
    }

    @Override
    public BadgeResult updateBadge(
            Long badgeId,
            UpdateBadgeCommand command
    ) {
        validateUpdateCommand(command);

        Badge badge = getNotDeletedBadge(badgeId);
        BadgeStatus status = parseStatus(command.status());

        if (badgePersistencePort.existsNotDeletedByNameExcludingId(
                command.badgeName(),
                badgeId
        )) {
            throw new ConflictException(
                    BadgeErrorCode.BADGE_NAME_ALREADY_EXISTS
            );
        }

        badge.update(
                command.badgeName(),
                command.description(),
                command.requiredPoint(),
                status
        );

        if (command.hasNewImage()) {
            replaceImage(badge, command);
        }

        Badge savedBadge = badgePersistencePort.save(badge);

        return toResult(savedBadge);
    }

    private void registerOldImageDeleteAfterCommit(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            badgeImageStoragePort.delete(objectName);
                        } catch (Exception e) {
                            log.error(
                                    "뱃지 이미지 교체 후 기존 이미지 삭제에 실패했습니다. objectName={}",
                                    objectName,
                                    e
                            );
                        }
                    }
                }
        );
    }

    private void replaceImage(
            Badge badge,
            UpdateBadgeCommand command
    ) {
        String oldObjectName = badge.getObjectName();

        var newImage = badgeImageStoragePort.upload(
                command.originalFileName(),
                command.contentType(),
                command.fileSize(),
                command.imageBytes()
        );

        registerImageRollbackCompensation(newImage.objectName());
        registerOldImageDeleteAfterCommit(oldObjectName);

        badge.replaceImage(
                newImage.originalFileName(),
                newImage.objectName(),
                newImage.contentType(),
                newImage.fileSize()
        );
    }

    private void validateUpdateCommand(UpdateBadgeCommand command) {
        if (command == null
                || command.badgeName() == null
                || command.badgeName().isBlank()
                || command.requiredPoint() == null
                || command.requiredPoint() < 0
                || command.status() == null
                || command.status().isBlank()) {
            throw new ValidationException(
                    BadgeErrorCode.BADGE_INVALID_INPUT
            );
        }
    }

    private BadgeStatus parseStatus(String status) {
        try {
            return BadgeStatus.valueOf(
                    status.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    BadgeErrorCode.BADGE_INVALID_INPUT
            );
        }
    }

    @Override
    public void deleteBadge(Long badgeId) {
        Badge badge = getNotDeletedBadge(badgeId);

        var equippedUserBadges =
                userBadgePersistencePort.findAllEquippedByBadgeId(badgeId);

        equippedUserBadges.forEach(userBadge -> userBadge.unequip());

        if (!equippedUserBadges.isEmpty()) {
            userBadgePersistencePort.saveAll(equippedUserBadges);
        }

        badge.delete();
        badgePersistencePort.save(badge);
    }

    private Badge getNotDeletedBadge(Long badgeId) {
        return badgePersistencePort.findNotDeletedById(badgeId)
                .orElseThrow(() -> new NotFoundException(
                        BadgeErrorCode.BADGE_NOT_FOUND
                ));
    }

    private BadgeResult toResult(Badge badge) {
        return new BadgeResult(
                badge.getBadgeId(),
                badge.getBadgeName(),
                badge.getDescription(),
                badge.getRequiredPoint(),
                generateImageUrl(badge.getObjectName()),
                badge.getStatus().name(),
                badge.getCreatedAt(),
                badge.getUpdatedAt()
        );
    }

    private String generateImageUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }

        return badgeImageStoragePort.generateAccessUrl(objectName);
    }
}
