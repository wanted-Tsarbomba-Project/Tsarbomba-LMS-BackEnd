package com.wanted.codebombalms.ranking.application.service;

import com.wanted.codebombalms.badge.application.port.BadgeImageStoragePort;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService implements RankingQueryUseCase {
    private final RankingQueryPort rankingQueryPort;
    private final BadgeImageStoragePort badgeImageStoragePort;

    @Override
    public RankingListResult getTotalPointRankings(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = normalizeSize(size);
        int offset = safePage * safeSize;

        return new RankingListResult(
                withBadgeImageUrls(
                        rankingQueryPort.findTotalPointRankings(offset, safeSize)
                )
        );
    }

    @Override
    public RankingItem getMyWeeklyPointRanking(Long userId) {
        LocalDateTime from = LocalDateTime.now().minusDays(7);

        return rankingQueryPort.findMyWeeklyPointRanking(userId, from)
                .map(this::withBadgeImageUrl)
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
                withBadgeImageUrls(
                        rankingQueryPort.findWeeklyPointRankings(from, offset, safeSize)
                )
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
        return rankingQueryPort.findMyTotalPointRanking(userId)
                .map(this::withBadgeImageUrl)
                .orElseThrow(() -> new NotFoundException(
                        RankingErrorCode.RANKING_NOT_FOUND
                ));
    }

    private List<RankingItem> withBadgeImageUrls(List<RankingItem> rankings) {
        return rankings.stream()
                .map(this::withBadgeImageUrl)
                .toList();
    }

    private RankingItem withBadgeImageUrl(RankingItem item) {
        return new RankingItem(
                item.rank(),
                item.userId(),
                item.name(),
                item.nickname(),
                item.badgeObjectName(),
                generateBadgeImageUrl(item.badgeObjectName()),
                item.weeklyPoint(),
                item.totalPoint()
        );
    }

    private String generateBadgeImageUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }

        return badgeImageStoragePort.generateAccessUrl(objectName);
    }
}
