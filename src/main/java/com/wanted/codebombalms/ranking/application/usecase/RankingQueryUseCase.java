package com.wanted.codebombalms.ranking.application.usecase;

import com.wanted.codebombalms.ranking.application.query.RankingItem;
import com.wanted.codebombalms.ranking.application.query.RankingListResult;

public interface RankingQueryUseCase {

    RankingListResult getTotalPointRankings();

    RankingListResult getWeeklyPointRankings();

    RankingItem getMyPointRanking(Long userId);
}
