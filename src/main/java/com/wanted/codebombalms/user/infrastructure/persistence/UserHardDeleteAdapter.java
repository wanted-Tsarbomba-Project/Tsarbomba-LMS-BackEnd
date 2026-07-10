package com.wanted.codebombalms.user.infrastructure.persistence;

import com.wanted.codebombalms.user.application.port.UserHardDeletePort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class UserHardDeleteAdapter implements UserHardDeletePort {

    // 1회 실행당 파기 대상 상한 — ID 목록 OOM 방지. 초과분은 익일 실행에서 처리
    private static final int MAX_BATCH = 1000;

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Long> findDeletedUserIdsBefore(LocalDateTime threshold) {
        // 소프트딜리트 1년 경과 + 아직 익명화 안 된 회원. @SQLRestriction(deleted_at IS NULL) 우회 위해 네이티브.
        // MAX_BATCH 는 코드 상수(int) — 문자열 결합해도 인젝션 없음. LIMIT 바인드 드라이버 이슈도 회피
        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery(
                        "SELECT user_id FROM users "
                      + "WHERE deleted_at IS NOT NULL AND deleted_at < :threshold AND anonymized_at IS NULL "
                      + "ORDER BY user_id LIMIT " + MAX_BATCH)
                .setParameter("threshold", threshold)
                .getResultList();

        List<Long> ids = rows.stream()
                .map(id -> ((Number) id).longValue())
                .toList();

        if (ids.size() == MAX_BATCH) {
            log.warn("회원 파기 대상이 배치 상한({})에 도달 — 잔여분은 다음 실행(익일 03시)에서 처리됩니다.", MAX_BATCH);
        }
        return ids;
    }

    @Override
    public void purgeByUserId(Long userId) {
        // 1) PII 자식 테이블 물리 삭제
        //    users 를 지우지 않으므로 CASCADE 가 안 걸림 → 명시적으로 삭제
        deleteFrom("login_history", userId);
        deleteFrom("trusted_devices", userId);
        deleteFrom("refresh_tokens", userId);
        deleteFrom("user_agreement", userId);

        // 2) users 본체는 '삭제' 대신 '익명화'
        //    활동데이터(submission·enrollment 등)의 RESTRICT FK 와 충돌 회피 +
        //    직접 식별정보 제거 + 재처리 방지 마커(anonymized_at) 기록
        em.createNativeQuery("""
                        UPDATE users SET
                            name          = '탈퇴회원',
                            phone         = NULL,
                            email         = CONCAT('#anonymized#', user_id),
                            password      = NULL,
                            bio           = NULL,
                            career        = NULL,
                            anonymized_at = NOW()
                        WHERE user_id = :userId
                        """)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    // 고정 테이블명만 사용 (사용자 입력 아님 — 인젝션 없음)
    private void deleteFrom(String table, Long userId) {
        em.createNativeQuery("DELETE FROM " + table + " WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
