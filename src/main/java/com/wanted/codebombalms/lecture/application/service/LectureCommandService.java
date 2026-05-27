package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.lecture.application.command.CreateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UpdateLectureCommand;
import com.wanted.codebombalms.lecture.application.policy.LectureCreationPolicy;
import com.wanted.codebombalms.lecture.application.port.CourseCatalogPort;
import com.wanted.codebombalms.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureCommandService implements LectureCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(LectureCommandService.class);

    private final LectureRepository lectureRepository;
    private final CourseCatalogPort courseCatalogPort;
    private final LectureCreationPolicy lectureCreationPolicy;

    @Override
    public Lecture createLecture(CreateLectureCommand command) {
        log.info("[LectureCommandService] create lecture - courseId: {}, title: {}", command.courseId(), command.title());

        Course course = courseCatalogPort.findCourse(command.courseId());
        lectureCreationPolicy.validate(course);
        validateLectureOrder(command.courseId(), null, command.lectureOrder());

        Lecture lecture = Lecture.create(
                course,
                command.title(),
                command.description(),
                command.videoUrl(),
                command.thumbnailUrl(),
                command.lectureOrder(),
                command.status()
        );

        return lectureRepository.save(lecture);
    }

    @Override
    public Lecture updateLecture(UpdateLectureCommand command) {
        log.info("[LectureCommandService] update lecture - lectureId: {}", command.lectureId());

        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(command.lectureId())
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));

        if (command.status() == LectureStatus.DELETED) {
            throw new ValidationException(LectureErrorCode.LECTURE_DELETE_STATUS_REQUIRES_DELETE);
        }
        validateLectureOrder(
                lecture.getCourse().getCourseId(),
                lecture.getLectureId(),
                command.lectureOrder()
        );

        lecture.update(
                command.title(),
                command.description(),
                command.videoUrl(),
                command.thumbnailUrl(),
                command.lectureOrder(),
                command.status()
        );

        return lectureRepository.save(lecture);
    }

    @Override
    public void deleteLecture(Long lectureId) {
        log.info("[LectureCommandService] delete lecture - lectureId: {}", lectureId);

        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));

        lecture.delete();
        lectureRepository.save(lecture);
    }

    private void validateLectureOrder(Long courseId, Long lectureId, Integer lectureOrder) {
        if (lectureOrder == null) {
            return;
        }

        boolean duplicated = lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId)
                .stream()
                .filter(lecture -> lectureId == null || !lecture.getLectureId().equals(lectureId))
                .anyMatch(lecture -> lecture.getLectureOrder().equals(lectureOrder));

        if (duplicated) {
            throw new ConflictException(LectureErrorCode.LECTURE_ORDER_DUPLICATED);
        }
    }
}
