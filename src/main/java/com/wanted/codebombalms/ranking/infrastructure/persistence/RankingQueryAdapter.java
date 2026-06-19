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
    public List<RankingItem> findTotalPointRankings(int offset, int size) {
        String sql = """
            select
                dense_rank() over (order by up.total_point desc) as ranking,
                u.user_id,
                u.name,
                u.nickname,
                null as badge_image_url,
                coalesce(weekly.weekly_point, 0) as weekly_point,
                up.total_point
            from user_point up
            join users u on u.user_id = up.user_id
            left join (
                select
                    ph.user_id,
                    sum(ph.point) as weekly_point
                from point_history ph
                where ph.created_at >= date_sub(now(), interval 7 day)
                group by ph.user_id
            ) weekly on weekly.user_id = u.user_id
            where u.deleted_at is null
              and u.role = 'STUDENT'
            order by ranking asc, u.user_id asc
            limit :size offset :offset
            """;

        return entityManager.createNativeQuery(sql)
                .setParameter("size", size)
                .setParameter("offset", offset)
                .getResultList()
                .stream()
                .map(row -> toRankingItem((Object[]) row))
                .toList();
    }

    @Override
    public List<RankingItem> findWeeklyPointRankings(LocalDateTime from, int offset, int size) {
        String sql = """
            select
                dense_rank() over (order by weekly.weekly_point desc) as ranking,
                u.user_id,
                u.name,
                u.nickname,
                null as badge_image_url,
                weekly.weekly_point,
                coalesce(up.total_point, 0) as total_point
            from (
                select
                    ph.user_id,
                    sum(ph.point) as weekly_point
                from point_history ph
                where ph.created_at >= :from
                group by ph.user_id
            ) weekly
            join users u on u.user_id = weekly.user_id
            left join user_point up on up.user_id = u.user_id
            where u.deleted_at is null
              and u.role = 'STUDENT'
            order by ranking asc, u.user_id asc
            limit :size offset :offset
            """;

        return entityManager.createNativeQuery(sql)
                .setParameter("from", from)
                .setParameter("size", size)
                .setParameter("offset", offset)
                .getResultList()
                .stream()
                .map(row -> toRankingItem((Object[]) row))
                .toList();
    }

    @Override
    public Optional<RankingItem> findMyTotalPointRanking(Long userId) {
        String sql = """
        select
            ranked.ranking,
            u.user_id,
            u.name,
            u.nickname,
            null as badge_image_url,
            coalesce(weekly.weekly_point, 0) as weekly_point,
            coalesce(up.total_point, 0) as total_point
        from users u
        left join user_point up on up.user_id = u.user_id
        left join (
            select
                ranked_user.user_id,
                ranked_user.ranking
            from (
                select
                    dense_rank() over (order by up2.total_point desc) as ranking,
                    up2.user_id
                from user_point up2
                join users u2 on u2.user_id = up2.user_id
                where u2.deleted_at is null
                  and u2.role = 'STUDENT'
            ) ranked_user
        ) ranked on ranked.user_id = u.user_id
        left join (
            select
                ph.user_id,
                sum(ph.point) as weekly_point
            from point_history ph
            where ph.created_at >= date_sub(now(), interval 7 day)
            group by ph.user_id
        ) weekly on weekly.user_id = u.user_id
        where u.user_id = :userId
          and u.deleted_at is null
          and u.role = 'STUDENT'
        """;

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("userId", userId);

        List<?> result = query.getResultList();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(toRankingItem((Object[]) result.get(0)));
    }

    @Override
    public Optional<RankingItem> findMyWeeklyPointRanking(
            Long userId,
            LocalDateTime from
    ) {
        String sql = """
        select
            ranked.ranking,
            u.user_id,
            u.name,
            u.nickname,
            null as badge_image_url,
            coalesce(my_weekly.weekly_point, 0) as weekly_point,
            coalesce(up.total_point, 0) as total_point
        from users u
        left join user_point up on up.user_id = u.user_id
        left join (
            select
                ph.user_id,
                sum(ph.point) as weekly_point
            from point_history ph
            where ph.created_at >= :from
            group by ph.user_id
        ) my_weekly on my_weekly.user_id = u.user_id
        left join (
            select
                weekly.user_id,
                dense_rank() over (order by weekly.weekly_point desc) as ranking
            from (
                select
                    ph.user_id,
                    sum(ph.point) as weekly_point
                from point_history ph
                where ph.created_at >= :from
                group by ph.user_id
            ) weekly
            join users ranked_user on ranked_user.user_id = weekly.user_id
            where ranked_user.deleted_at is null
              and ranked_user.role = 'STUDENT'
        ) ranked on ranked.user_id = u.user_id
        where u.user_id = :userId
          and u.deleted_at is null
          and u.role = 'STUDENT'
        """;

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("from", from)
                .setParameter("userId", userId);

        List<?> result = query.getResultList();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(toRankingItem((Object[]) result.get(0)));
    }

    private RankingItem toRankingItem(Object[] row) {
        return new RankingItem(
                row[0] == null ? null : ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue(),
                (String) row[2],
                (String) row[3],
                (String) row[4],
                ((Number) row[5]).intValue(),
                ((Number) row[6]).intValue()
        );
    }
}
