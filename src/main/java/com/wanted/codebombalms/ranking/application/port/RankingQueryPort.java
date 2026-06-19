package com.wanted.codebombalms.ranking.application.port;

import com.wanted.codebombalms.ranking.application.query.RankingItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RankingQueryPort {
    List<RankingItem> findTotalPointRankings(int offset, int size);
    List<RankingItem> findWeeklyPointRankings(LocalDateTime from, int offset, int size);
    Optional<RankingItem> findMyTotalPointRanking(Long userId);
    Optional<RankingItem> findMyWeeklyPointRanking(Long userId, LocalDateTime from);
}
