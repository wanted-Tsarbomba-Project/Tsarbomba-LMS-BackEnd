package com.wanted.codebombalms.badge.infrastructure.persistence;

import com.wanted.codebombalms.badge.domain.model.UserBadge;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserBadgeBulkInserter {

    private final JdbcTemplate jdbcTemplate;

    // 배지 동기화는 한 번에 여러 user_badge 행을 저장한다.
    // DB 왕복과 Hikari 커넥션 점유를 줄이기 위해 multi-row insert를 사용한다.
    public List<UserBadge> insertIgnoreAll(List<UserBadge> userBadges) {
        if (userBadges.isEmpty()) {
            return List.of();
        }

        String sql = """
                insert into user_badge
                  (user_id, badge_id, earned_at, is_equipped)
                values
                """
                + String.join(
                ",",
                Collections.nCopies(
                        userBadges.size(),
                        "(?, ?, ?, ?)"
                )
        )
                + """
                
                on duplicate key update
                  badge_id = badge_id
                """;

        jdbcTemplate.update(sql, toInsertParams(userBadges));

        return findExistingUserBadges(userBadges);
    }

    private Object[] toInsertParams(List<UserBadge> userBadges) {
        List<Object> params = new ArrayList<>(userBadges.size() * 4);

        for (UserBadge userBadge : userBadges) {
            params.add(userBadge.getUserId());
            params.add(userBadge.getBadgeId());
            params.add(Timestamp.valueOf(userBadge.getEarnedAt()));
            params.add(userBadge.isEquipped());
        }

        return params.toArray();
    }

    private List<UserBadge> findExistingUserBadges(List<UserBadge> userBadges) {
        String sql = """
                select user_badge_id, user_id, badge_id, earned_at, is_equipped
                  from user_badge
                 where (user_id, badge_id) in (
                """
                + String.join(
                ",",
                Collections.nCopies(
                        userBadges.size(),
                        "(?, ?)"
                )
        )
                + ")";

        return jdbcTemplate.query(
                sql,
                toFindParams(userBadges),
                (rs, rowNum) -> UserBadge.restore(
                        rs.getLong("user_badge_id"),
                        rs.getLong("user_id"),
                        rs.getLong("badge_id"),
                        rs.getTimestamp("earned_at").toLocalDateTime(),
                        rs.getBoolean("is_equipped")
                )
        );
    }

    private Object[] toFindParams(List<UserBadge> userBadges) {
        List<Object> params = new ArrayList<>(userBadges.size() * 2);

        for (UserBadge userBadge : userBadges) {
            params.add(userBadge.getUserId());
            params.add(userBadge.getBadgeId());
        }

        return params.toArray();
    }
}
