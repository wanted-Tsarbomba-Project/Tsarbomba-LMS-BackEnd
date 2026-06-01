package com.wanted.codebombalms.ranking.application.query;

public record RankingItem(
        Integer rank,
        Long userId,
        String name,
        String nickname,
        String badgeImageUrl,
        Integer weeklyPoint,
        Integer totalPoint
) {
}
