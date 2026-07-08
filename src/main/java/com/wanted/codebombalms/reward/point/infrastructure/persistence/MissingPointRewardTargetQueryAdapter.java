package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import com.wanted.codebombalms.reward.point.application.port.FindMissingPointRewardTargetsPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MissingPointRewardTargetQueryAdapter
        implements FindMissingPointRewardTargetsPort {

    private final EntityManager entityManager;

    @Override
    public List<MissingPointRewardTarget> findTargets(int limit) {
        List<Object[]> rows = entityManager.createQuery("""
                        select s.userId, p.problemId, s.submissionId, p.point
                        from SubmissionJpaEntity s
                        join s.problem p
                        where s.isCorrect = true
                          and not exists (
                              select 1
                              from PointRewardTaskJpaEntity task
                              where task.submissionId = s.submissionId
                          )
                          and not exists (
                              select 1
                              from PointHistoryJpaEntity history
                              where history.userId = s.userId
                                and history.problemId = p.problemId
                          )
                        order by s.submittedAt asc, s.submissionId asc
                        """, Object[].class)
                .setMaxResults(limit)
                .getResultList();

        return rows.stream()
                .map(row -> new MissingPointRewardTarget(
                        (Long) row[0],
                        (Long) row[1],
                        (Long) row[2],
                        (Integer) row[3]
                ))
                .toList();
    }
}
