package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SpringDataProblemSetRepository extends JpaRepository<ProblemSetJpaEntity, Long> {

    List<ProblemSetJpaEntity> findByCategory_CategoryIdAndStatusOrderByProblemSetIdAsc(
            Long categoryId,
            ProblemSetStatus status
    );

    List<ProblemSetJpaEntity> findByStatusOrderByProblemSetIdAsc(
            ProblemSetStatus status
    );

    @Transactional
    default int hardDeleteByDeletedAtBefore(LocalDateTime threshold) {
        List<Long> problemSetIds = findHardDeleteTargetIds(threshold);
        if (problemSetIds.isEmpty()) {
            return 0;
        }

        deleteChatMessagesByProblemSetIds(problemSetIds);
        deleteChatRoomsByProblemSetIds(problemSetIds);
        deleteLectureProblemProgressesByProblemSetIds(problemSetIds);
        deleteLectureProblemSetsByProblemSetIds(problemSetIds);
        deleteSubmissionTestResultsByProblemSetIds(problemSetIds);
        deleteSubmissionsByProblemSetIds(problemSetIds);
        deleteProblemTestCasesByProblemSetIds(problemSetIds);
        deleteProblemHintsByProblemSetIds(problemSetIds);
        deleteProblemDatasetsByProblemSetIds(problemSetIds);
        deleteProblemProgressesByProblemSetIds(problemSetIds);
        deleteProblemsByProblemSetIds(problemSetIds);
        return deleteProblemSetsByIds(problemSetIds);
    }

    @Query("""
            select ps.problemSetId
            from ProblemSetJpaEntity ps
            where ps.deletedAt is not null
              and ps.deletedAt < :threshold
            """)
    List<Long> findHardDeleteTargetIds(@Param("threshold") LocalDateTime threshold);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ChatMessageJpaEntity m
            where m.roomId in (
                select r.id
                from ChatRoomJpaEntity r
                where r.problemSetId in :problemSetIds
            )
            """)
    int deleteChatMessagesByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ChatRoomJpaEntity r
            where r.problemSetId in :problemSetIds
            """)
    int deleteChatRoomsByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from LectureProblemProgressJpaEntity p
            where p.lectureProblemSetId in (
                select cps.lectureProblemSetId
                from LectureProblemSetJpaEntity cps
                where cps.problemSetId in :problemSetIds
            )
            """)
    int deleteLectureProblemProgressesByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from LectureProblemSetJpaEntity cps
            where cps.problemSetId in :problemSetIds
            """)
    int deleteLectureProblemSetsByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from SubmissionTestResultJpaEntity r
            where r.submission.problem.problemSet.problemSetId in :problemSetIds
               or r.testCase.problem.problemSet.problemSetId in :problemSetIds
            """)
    int deleteSubmissionTestResultsByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from SubmissionJpaEntity s
            where s.problem.problemSet.problemSetId in :problemSetIds
            """)
    int deleteSubmissionsByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ProblemTestCaseJpaEntity tc
            where tc.problem.problemSet.problemSetId in :problemSetIds
            """)
    int deleteProblemTestCasesByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ProblemHintJpaEntity h
            where h.problem.problemSet.problemSetId in :problemSetIds
            """)
    int deleteProblemHintsByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ProblemDatasetJpaEntity d
            where d.problemSet.problemSetId in :problemSetIds
            """)
    int deleteProblemDatasetsByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ProgressJpaEntity p
            where p.problemSet.problemSetId in :problemSetIds
            """)
    int deleteProblemProgressesByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ProblemJpaEntity p
            where p.problemSet.problemSetId in :problemSetIds
            """)
    int deleteProblemsByProblemSetIds(@Param("problemSetIds") List<Long> problemSetIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ProblemSetJpaEntity ps
            where ps.problemSetId in :problemSetIds
            """)
    int deleteProblemSetsByIds(@Param("problemSetIds") List<Long> problemSetIds);


}
