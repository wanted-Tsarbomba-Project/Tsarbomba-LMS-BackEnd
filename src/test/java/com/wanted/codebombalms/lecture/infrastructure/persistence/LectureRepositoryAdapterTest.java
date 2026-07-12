package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.lecture.application.port.CourseCatalogPort;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class LectureRepositoryAdapterTest {

    @Mock
    private SpringDataLectureRepository springDataLectureRepository;

    @Mock
    private CourseCatalogPort courseCatalogPort;

    @InjectMocks
    private LectureRepositoryAdapter lectureRepositoryAdapter;

    @Test
    void updateLectureOrders_savesTemporaryOrdersBeforeFinalOrders() {
        Course course = createCourse(1L);
        Lecture first = createLecture(1L, course, 1);
        Lecture second = createLecture(2L, course, 2);
        LectureJpaEntity firstEntity = LectureJpaEntity.from(first);
        LectureJpaEntity secondEntity = LectureJpaEntity.from(second);

        given(springDataLectureRepository.findById(1L)).willReturn(Optional.of(firstEntity));
        given(springDataLectureRepository.findById(2L)).willReturn(Optional.of(secondEntity));
        given(springDataLectureRepository.save(any(LectureJpaEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        lectureRepositoryAdapter.updateLectureOrders(
                List.of(first, second),
                Map.of(1L, 2, 2L, 1)
        );

        InOrder order = inOrder(springDataLectureRepository);
        order.verify(springDataLectureRepository).findById(1L);
        order.verify(springDataLectureRepository).save(firstEntity);
        order.verify(springDataLectureRepository).findById(2L);
        order.verify(springDataLectureRepository).save(secondEntity);
        order.verify(springDataLectureRepository).flush();
        order.verify(springDataLectureRepository).findById(1L);
        order.verify(springDataLectureRepository).save(firstEntity);
        order.verify(springDataLectureRepository).findById(2L);
        order.verify(springDataLectureRepository).save(secondEntity);

        assertEquals(2, firstEntity.getLectureOrder());
        assertEquals(1, secondEntity.getLectureOrder());
    }

    private Course createCourse(Long courseId) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setInstructorId(10L);
        course.setTitle("Java");
        course.setCreatedAt(LocalDateTime.now());
        return course;
    }

    private Lecture createLecture(Long lectureId, Course course, Integer lectureOrder) {
        return Lecture.restore(
                lectureId,
                course,
                "Java " + lectureId,
                "description",
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                "thumbnail.png",
                3001L,
                LectureStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                lectureOrder
        );
    }
}
