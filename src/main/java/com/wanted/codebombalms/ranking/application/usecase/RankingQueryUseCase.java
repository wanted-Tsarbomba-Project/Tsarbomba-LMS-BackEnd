package com.wanted.codebombalms.ranking.application.usecase;

import com.wanted.codebombalms.ranking.application.query.RankingItem;
import com.wanted.codebombalms.ranking.application.query.RankingListResult;

public interface RankingQueryUseCase {

    RankingListResult getTotalPointRankings(int page, int size);

    RankingListResult getWeeklyPointRankings(int page, int size);

    RankingItem getMyPointRanking(Long userId);
}
