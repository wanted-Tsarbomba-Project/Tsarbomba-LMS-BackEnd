package com.wanted.codebombalms.ranking.application.service;

import com.wanted.codebombalms.ranking.application.port.RankingQueryPort;
import com.wanted.codebombalms.ranking.application.query.RankingItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingReader {

    private final RankingQueryPort rankingQueryPort;

    public List<RankingItem> findTotalPointRankings(int offset, int size) {
        return rankingQueryPort.findTotalPointRankings(offset, size);
    }

    public List<RankingItem> findWeeklyPointRankings(
            LocalDateTime from,
            int offset,
            int size
    ) {
        return rankingQueryPort.findWeeklyPointRankings(from, offset, size);
    }

    public Optional<RankingItem> findMyTotalPointRanking(Long userId) {
        return rankingQueryPort.findMyTotalPointRanking(userId);
    }

    public Optional<RankingItem> findMyWeeklyPointRanking(
            Long userId,
            LocalDateTime from
    ) {
        return rankingQueryPort.findMyWeeklyPointRanking(userId, from);
    }
}
