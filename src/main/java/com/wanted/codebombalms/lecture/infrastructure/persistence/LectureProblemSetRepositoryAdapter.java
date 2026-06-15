package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import com.wanted.codebombalms.lecture.domain.repository.LectureProblemSetRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LectureProblemSetRepositoryAdapter implements LectureProblemSetRepository {

    private final SpringDataLectureProblemSetRepository springDataLectureProblemSetRepository;

    @Override
    public LectureProblemSet save(LectureProblemSet lectureProblemSet) {
        LectureProblemSetJpaEntity entity = lectureProblemSet.getLectureProblemSetId() == null
                ? LectureProblemSetJpaEntity.from(lectureProblemSet)
                : springDataLectureProblemSetRepository.findById(lectureProblemSet.getLectureProblemSetId())
                .map(found -> {
                    found.apply(lectureProblemSet);
                    return found;
                })
                .orElseGet(() -> LectureProblemSetJpaEntity.from(lectureProblemSet));

        return springDataLectureProblemSetRepository.save(entity).toDomain();
    }

    @Override
    public List<LectureProblemSet> findByCourseId(Long courseId) {
        return springDataLectureProblemSetRepository.findActiveByCourseId(courseId)
                .stream()
                .map(LectureProblemSetJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<LectureProblemSet> findByCourseIdAndRole(Long courseId, LectureProblemSetRole role) {
        return springDataLectureProblemSetRepository.findActiveByCourseIdAndRole(courseId, role)
                .stream()
                .map(LectureProblemSetJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<LectureProblemSet> findByLectureId(Long lectureId) {
        return springDataLectureProblemSetRepository.findActiveByLectureIdOrderByDisplayOrderAsc(lectureId)
                .stream()
                .map(LectureProblemSetJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<LectureProblemSet> findById(Long lectureProblemSetId) {
        return springDataLectureProblemSetRepository.findActiveById(lectureProblemSetId)
                .map(LectureProblemSetJpaEntity::toDomain);
    }
}
