package com.wanted.codebombalms.ranking.application.query;

public record RankingItem(
        Integer rank,
        Long userId,
        String nickname,
        Integer point
) {
}
