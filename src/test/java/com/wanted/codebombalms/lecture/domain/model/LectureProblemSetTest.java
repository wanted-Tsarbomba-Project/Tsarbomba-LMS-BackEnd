package com.wanted.codebombalms.lecture.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LectureProblemSetTest {

    @Test
    void create_createsNewMainProblemSetConnection() {
        LectureProblemSet lectureProblemSet = LectureProblemSet.create(
                10L,
                100L,
                1000L,
                LectureProblemSetRole.MAIN,
                1
        );

        assertNull(lectureProblemSet.getLectureProblemSetId());
        assertEquals(10L, lectureProblemSet.getCourseId());
        assertEquals(100L, lectureProblemSet.getLectureId());
        assertEquals(1000L, lectureProblemSet.getProblemSetId());
        assertEquals(LectureProblemSetRole.MAIN, lectureProblemSet.getRole());
        assertEquals(1, lectureProblemSet.getDisplayOrder());
    }

    @Test
    void restore_restoresExistingFinalProblemSetConnection() {
        LectureProblemSet lectureProblemSet = LectureProblemSet.restore(
                1L,
                10L,
                null,
                1000L,
                LectureProblemSetRole.FINAL,
                1
        );

        assertEquals(1L, lectureProblemSet.getLectureProblemSetId());
        assertEquals(10L, lectureProblemSet.getCourseId());
        assertNull(lectureProblemSet.getLectureId());
        assertEquals(1000L, lectureProblemSet.getProblemSetId());
        assertEquals(LectureProblemSetRole.FINAL, lectureProblemSet.getRole());
        assertEquals(1, lectureProblemSet.getDisplayOrder());
    }

    @Test
    void restore_rejectsNullIdentifier() {
        assertThrows(
                NullPointerException.class,
                () -> LectureProblemSet.restore(
                        null,
                        10L,
                        100L,
                        1000L,
                        LectureProblemSetRole.MAIN,
                        1
                )
        );
    }
}
