package com.wanted.codebombalms.ranking.infrastructure.persistence;

import com.wanted.codebombalms.ranking.application.port.RankingQueryPort;
import com.wanted.codebombalms.ranking.application.query.RankingItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class RankingQueryAdapter implements RankingQueryPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<RankingItem> findTotalPointRankings() {
        String sql = """
                select
                    dense_rank() over (order by up.total_point desc) as ranking,
                    u.user_id,
                    u.nickname,
                    up.total_point
                from user_point up
                join users u on u.user_id = up.user_id
                where u.deleted_at is null
                  and u.role = 'STUDENT'
                order by ranking asc, u.user_id asc
                """;

        return entityManager.createNativeQuery(sql)
                .getResultList()
                .stream()
                .map(row -> toRankingItem((Object[]) row))
                .toList();
    }

    @Override
    public List<RankingItem> findWeeklyPointRankings(LocalDateTime from) {
        String sql = """
                select
                    dense_rank() over (order by sum(ph.point) desc) as ranking,
                    u.user_id,
                    u.nickname,
                    sum(ph.point) as total_point
                from point_history ph
                join users u on u.user_id = ph.user_id
                where ph.created_at >= :from
                  and u.deleted_at is null
                  and u.role = 'STUDENT'
                group by u.user_id, u.nickname
                order by ranking asc, u.user_id asc
                """;

        return entityManager.createNativeQuery(sql)
                .setParameter("from", from)
                .getResultList()
                .stream()
                .map(row -> toRankingItem((Object[]) row))
                .toList();
    }

    @Override
    public Optional<RankingItem> findMyTotalPointRanking(Long userId) {
        String sql = """
                select *
                from (
                    select
                        dense_rank() over (order by up.total_point desc) as ranking,
                        u.user_id,
                        u.nickname,
                        up.total_point
                    from user_point up
                    join users u on u.user_id = up.user_id
                    where u.deleted_at is null
                      and u.role = 'STUDENT'
                ) ranked
                where ranked.user_id = :userId
                """;

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("userId", userId);

        List<?> result = query.getResultList();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(toRankingItem((Object[]) result.get(0)));
    }

    private RankingItem toRankingItem(Object[] row) {
        return new RankingItem(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue(),
                (String) row[2],
                ((Number) row[3]).intValue()
        );
    }
}
