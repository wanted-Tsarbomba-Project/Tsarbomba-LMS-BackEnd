package com.wanted.codebombalms.lecture.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LectureProblemSetTest {

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
