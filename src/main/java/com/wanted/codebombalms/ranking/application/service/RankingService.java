package com.wanted.codebombalms.ranking.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.ranking.application.port.RankingQueryPort;
import com.wanted.codebombalms.ranking.application.query.RankingItem;
import com.wanted.codebombalms.ranking.application.query.RankingListResult;
import com.wanted.codebombalms.ranking.application.usecase.RankingQueryUseCase;
import com.wanted.codebombalms.ranking.exception.RankingErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService implements RankingQueryUseCase {
    private final RankingQueryPort rankingQueryPort;

    @Override
    public RankingListResult getTotalPointRankings(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = normalizeSize(size);
        int offset = safePage * safeSize;

        return new RankingListResult(
                rankingQueryPort.findTotalPointRankings(offset, safeSize)
        );
    }

    @Override
    public RankingItem getMyWeeklyPointRanking(Long userId) {
        LocalDateTime from = LocalDateTime.now().minusDays(7);

        return rankingQueryPort.findMyWeeklyPointRanking(userId, from)
                .orElseThrow(() -> new NotFoundException(
                        RankingErrorCode.RANKING_NOT_FOUND
                ));
    }

    @Override
    public RankingListResult getWeeklyPointRankings(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = normalizeSize(size);
        int offset = safePage * safeSize;
        LocalDateTime from = LocalDateTime.now().minusDays(7);

        return new RankingListResult(
                rankingQueryPort.findWeeklyPointRankings(from, offset, safeSize)
        );
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 20;
        }

        return Math.min(size, 100);
    }

    @Override
    public RankingItem getMyPointRanking(Long userId) {

        return rankingQueryPort.findMyTotalPointRanking(userId).
                orElseThrow(() -> new NotFoundException(RankingErrorCode.RANKING_NOT_FOUND));
    }
}
