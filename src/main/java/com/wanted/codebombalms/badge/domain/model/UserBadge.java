package com.wanted.codebombalms.badge.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {

    private Long userBadgeId;
    private Long userId;
    private Long badgeId;
    private LocalDateTime earnedAt;
    private boolean equipped;

    public static UserBadge earn(
            Long userId,
            Long badgeId
    ) {
        UserBadge userBadge = new UserBadge();
        userBadge.userId = userId;
        userBadge.badgeId = badgeId;
        userBadge.earnedAt = LocalDateTime.now();
        userBadge.equipped = false;
        return userBadge;
    }

    public static UserBadge restore(
            Long userBadgeId,
            Long userId,
            Long badgeId,
            LocalDateTime earnedAt,
            boolean equipped
    ) {
        return new UserBadge(
                userBadgeId,
                userId,
                badgeId,
                earnedAt,
                equipped
        );
    }

    public void equip() {
        this.equipped = true;
    }

    public void unequip() {
        this.equipped = false;
    }
}
