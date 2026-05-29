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
    public RankingListResult getTotalPointRankings() {
        return new RankingListResult(
                rankingQueryPort.findTotalPointRankings()
        );
    }

    @Override
    public RankingListResult getWeeklyPointRankings() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);

        return new RankingListResult(
                rankingQueryPort.findWeeklyPointRankings(from)
        );
    }

    @Override
    public RankingItem getMyPointRanking(Long userId) {

        return rankingQueryPort.findMyTotalPointRanking(userId).
                orElseThrow(() -> new NotFoundException(RankingErrorCode.RANKING_NOT_FOUND));
    }
}
