package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseProblemStep;
import com.wanted.codebombalms.course.domain.repository.CourseProblemStepRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CourseProblemStepRepositoryAdapter implements CourseProblemStepRepository {

    private final SpringDataCourseProblemStepRepository springDataCourseProblemStepRepository;
    private final SpringDataCourseProblemSetRepository springDataCourseProblemSetRepository;

    @Override
    public CourseProblemStep save(CourseProblemStep courseProblemStep) {
        CourseProblemSetJpaEntity courseProblemSet = springDataCourseProblemSetRepository
                .findById(courseProblemStep.getCourseProblemSetId())
                .orElseThrow();

        CourseProblemStepJpaEntity entity = courseProblemStep.getCourseProblemStepId() == null
                ? CourseProblemStepJpaEntity.from(courseProblemStep, courseProblemSet)
                : springDataCourseProblemStepRepository.findById(courseProblemStep.getCourseProblemStepId())
                .map(found -> {
                    found.apply(courseProblemStep, courseProblemSet);
                    return found;
                })
                .orElseGet(() -> CourseProblemStepJpaEntity.from(courseProblemStep, courseProblemSet));

        return springDataCourseProblemStepRepository.save(entity).toDomain();
    }

    @Override
    public List<CourseProblemStep> findByLectureId(Long lectureId) {
        return springDataCourseProblemStepRepository.findByLectureIdOrderByStepOrderAsc(lectureId)
                .stream()
                .map(CourseProblemStepJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<CourseProblemStep> findByCourseProblemSetId(Long courseProblemSetId) {
        return springDataCourseProblemStepRepository
                .findByCourseProblemSet_CourseProblemSetIdOrderByStepOrderAsc(courseProblemSetId)
                .stream()
                .map(CourseProblemStepJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteByCourseProblemSetId(Long courseProblemSetId) {
        springDataCourseProblemStepRepository.deleteAll(
                springDataCourseProblemStepRepository
                        .findByCourseProblemSet_CourseProblemSetIdOrderByStepOrderAsc(courseProblemSetId)
        );
    }
}
