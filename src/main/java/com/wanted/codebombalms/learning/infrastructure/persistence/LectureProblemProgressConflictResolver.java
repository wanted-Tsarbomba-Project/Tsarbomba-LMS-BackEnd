package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 최초 진입 INSERT가 유니크 제약 위반으로 실패한 직후에는 원래 트랜잭션의 영속성 컨텍스트를
 * 그대로 재사용하면 안 되므로(플러시 예외 이후 동일 세션 재사용 위험), 별도 트랜잭션에서 재조회한다.
 */
@Component
@RequiredArgsConstructor
public class LectureProblemProgressConflictResolver {

    private final SpringDataLectureProblemProgressRepository springDataLectureProblemProgressRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<LectureProblemProgress> findAfterInsertConflict(Long userId, Long lectureProblemSetId) {
        return springDataLectureProblemProgressRepository
                .findByUserIdAndLectureProblemSetId(userId, lectureProblemSetId)
                .map(LectureProblemProgressJpaEntity::toDomain);
    }
}
