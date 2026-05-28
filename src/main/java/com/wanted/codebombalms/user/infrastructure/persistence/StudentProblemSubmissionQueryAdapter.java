package com.wanted.codebombalms.user.infrastructure.persistence;

import com.wanted.codebombalms.user.application.port.StudentProblemSubmissionQueryPort;
import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionItem;
import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StudentProblemSubmissionQueryAdapter implements StudentProblemSubmissionQueryPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<StudentProblemSubmissionItem> findByCondition(StudentProblemSubmissionQuery query) {
        String jpql = """
                select
                    ps.problemSetId as problemSetId,
                    ps.title as problemSetTitle,
                    ps.description as problemSetDescription,
                    ps.difficulty as problemSetDifficulty,
                    ps.totalProblemCount as totalProblemCount,

                    p.problemId as problemId,
                    p.title as problemTitle,
                    p.problemType as problemType,
                    p.difficulty as problemDifficulty,
                    p.problemOrder as problemOrder,
                    p.point as point,
                    p.attemptLimit as attemptLimit,
                    p.isRetriable as retriable,

                    s.submissionId as submissionId,
                    s.submittedAnswer as submittedAnswer,
                    s.submittedCode as submittedCode,
                    s.isCorrect as correct,
                    coalesce(ph.point, 0) as earnedPoint,
                    s.attemptNo as attemptNo,
                    s.submittedAt as submittedAt,

                    case
                        when s.isCorrect = true then 'CORRECT'
                        when s.isCorrect = false then 'INCORRECT'
                        else 'SUBMITTED'
                    end as submissionStatus

                from SubmissionJpaEntity s
                join s.problem p
                join p.problemSet ps
                left join PointHistoryJpaEntity ph
                    on ph.submissionId = s.submissionId

                where s.userId = :userId
                  and (:problemSetId is null or ps.problemSetId = :problemSetId)
                  and (:problemId is null or p.problemId = :problemId)
                  and (:correctOnly = false or s.isCorrect = true)

                order by
                    ps.problemSetId asc,
                    p.problemOrder asc,
                    s.attemptNo asc,
                    s.submittedAt asc
                """;

        return entityManager.createQuery(jpql, Tuple.class)
                .setParameter("userId", query.userId())
                .setParameter("problemSetId", query.problemSetId())
                .setParameter("problemId", query.problemId())
                .setParameter("correctOnly", query.correctOnlyValue())
                .getResultList()
                .stream()
                .map(this::toItem)
                .toList();
    }

    private StudentProblemSubmissionItem toItem(Tuple tuple) {
        return new StudentProblemSubmissionItem(
                tuple.get("problemSetId", Long.class),
                tuple.get("problemSetTitle", String.class),
                tuple.get("problemSetDescription", String.class),
                tuple.get("problemSetDifficulty", String.class),
                tuple.get("totalProblemCount", Integer.class),

                tuple.get("problemId", Long.class),
                tuple.get("problemTitle", String.class),
                tuple.get("problemType", String.class),
                tuple.get("problemDifficulty", String.class),
                tuple.get("problemOrder", Integer.class),
                tuple.get("point", Integer.class),
                tuple.get("attemptLimit", Integer.class),
                tuple.get("retriable", Boolean.class),

                tuple.get("submissionId", Long.class),
                tuple.get("submittedAnswer", String.class),
                tuple.get("submittedCode", String.class),
                tuple.get("correct", Boolean.class),
                tuple.get("earnedPoint", Integer.class),
                tuple.get("attemptNo", Integer.class),
                tuple.get("submittedAt", LocalDateTime.class),
                tuple.get("submissionStatus", String.class)
        );
    }
}
