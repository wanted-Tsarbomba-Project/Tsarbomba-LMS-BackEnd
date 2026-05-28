package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.course.domain.repository.CourseProblemSetRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CourseProblemSetRepositoryAdapter implements CourseProblemSetRepository {

    private final SpringDataCourseProblemSetRepository springDataCourseProblemSetRepository;
    private final SpringDataCourseRepository springDataCourseRepository;

    @Override
    public CourseProblemSet save(CourseProblemSet courseProblemSet) {
        CourseJpaEntity course = springDataCourseRepository.findById(courseProblemSet.getCourseId())
                .orElseThrow();

        CourseProblemSetJpaEntity entity = courseProblemSet.getCourseProblemSetId() == null
                ? CourseProblemSetJpaEntity.from(courseProblemSet, course)
                : springDataCourseProblemSetRepository.findById(courseProblemSet.getCourseProblemSetId())
                .map(found -> {
                    found.apply(courseProblemSet, course);
                    return found;
                })
                .orElseGet(() -> CourseProblemSetJpaEntity.from(courseProblemSet, course));

        return springDataCourseProblemSetRepository.save(entity).toDomain();
    }

    @Override
    public List<CourseProblemSet> findByCourseId(Long courseId) {
        return springDataCourseProblemSetRepository.findByCourse_CourseIdAndDeletedAtIsNull(courseId)
                .stream()
                .map(CourseProblemSetJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<CourseProblemSet> findByCourseIdAndRole(Long courseId, CourseProblemSetRole role) {
        return springDataCourseProblemSetRepository.findByCourse_CourseIdAndRoleAndDeletedAtIsNull(courseId, role)
                .stream()
                .map(CourseProblemSetJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<CourseProblemSet> findByLectureId(Long lectureId) {
        return springDataCourseProblemSetRepository.findByLectureIdAndDeletedAtIsNullOrderByDisplayOrderAsc(lectureId)
                .stream()
                .map(CourseProblemSetJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<CourseProblemSet> findById(Long courseProblemSetId) {
        return springDataCourseProblemSetRepository.findByCourseProblemSetIdAndDeletedAtIsNull(courseProblemSetId)
                .map(CourseProblemSetJpaEntity::toDomain);
    }

    @Override
    public void deleteByCourseId(Long courseId) {
        List<CourseProblemSetJpaEntity> problemSets =
                springDataCourseProblemSetRepository.findByCourse_CourseIdAndDeletedAtIsNull(courseId);

        problemSets.forEach(CourseProblemSetJpaEntity::delete);
        springDataCourseProblemSetRepository.saveAll(problemSets);
    }

    @Override
    public void deleteByLectureId(Long lectureId) {
        List<CourseProblemSetJpaEntity> problemSets =
                springDataCourseProblemSetRepository.findByLectureIdAndDeletedAtIsNullOrderByDisplayOrderAsc(lectureId);

        problemSets.forEach(CourseProblemSetJpaEntity::delete);
        springDataCourseProblemSetRepository.saveAll(problemSets);
    }
}
