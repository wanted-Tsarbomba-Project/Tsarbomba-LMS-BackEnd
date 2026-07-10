package com.wanted.codebombalms.user.infrastructure.persistence;

import com.wanted.codebombalms.user.application.port.UserHardDeletePort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class UserHardDeleteAdapter implements UserHardDeletePort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Long> findDeletedUserIdsBefore(LocalDateTime threshold) {
        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery("""
                        SELECT user_id FROM users
                        WHERE deleted_at IS NOT NULL AND deleted_at < :threshold
                        """)
                .setParameter("threshold", threshold)
                .getResultList();

        return rows.stream()
                .map(id -> ((Number) id).longValue())
                .toList();
    }

    @Override
    public void purgeByUserId(Long userId) {
        // 활동데이터(submissions·progress·points·badge·enrollment·chat)는 A안 = 익명화(유지).
        // users 행이 사라지면 user_id 는 식별 불가 → 사실상 익명화됨.
        deleteFrom("login_history", userId);
        deleteFrom("trusted_devices", userId);
        deleteFrom("refresh_tokens", userId);
        deleteFrom("user_agreement", userId);
        deleteFrom("users", userId);          // PII 앵커 — 마지막에 삭제
    }

    // 고정 테이블명만 사용 (사용자 입력 아님 — 인젝션 없음)
    private void deleteFrom(String table, Long userId) {
        em.createNativeQuery("DELETE FROM " + table + " WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
