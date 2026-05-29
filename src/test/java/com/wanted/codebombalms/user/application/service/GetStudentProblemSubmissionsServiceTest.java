package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.application.port.StudentProblemSubmissionQueryPort;
import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionQuery;
import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionResult;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStudentProblemSubmissionsService unit test")
class GetStudentProblemSubmissionsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentProblemSubmissionQueryPort queryPort;

    @InjectMocks
    private GetStudentProblemSubmissionsService service;

    private final StudentProblemSubmissionQuery query =
            new StudentProblemSubmissionQuery(1L, null, null, null, 0, 20);

    @Test
    @DisplayName("returns submissions for valid student")
    void returns_submissions_for_valid_student() {
        // given
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(createStudent(false)));
        given(queryPort.findByCondition(query)).willReturn(List.of());

        // when
        StudentProblemSubmissionResult result = service.getStudentProblemSubmissions(query);

        // then
        assertNotNull(result);
        verify(queryPort).findByCondition(query);
    }

    @Test
    @DisplayName("throws STUDENT_NOT_FOUND when user does not exist")
    void throws_when_user_not_found() {
        given(userRepository.findByUserId(1L)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.getStudentProblemSubmissions(query)
        );
        assertEquals(UserErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode());
        verify(queryPort, never()).findByCondition(any());
    }

    @Test
    @DisplayName("throws STUDENT_NOT_FOUND when user is not student")
    void throws_when_user_is_not_student() {
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(createUser(UserRole.ADMIN, false)));

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.getStudentProblemSubmissions(query)
        );
        assertEquals(UserErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode());
        verify(queryPort, never()).findByCondition(any());
    }

    @Test
    @DisplayName("throws STUDENT_NOT_FOUND when student is deleted")
    void throws_when_student_is_deleted() {
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(createStudent(true)));

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.getStudentProblemSubmissions(query)
        );
        assertEquals(UserErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode());
        verify(queryPort, never()).findByCondition(any());
    }


    private User createStudent(boolean deleted) {
        return createUser(UserRole.STUDENT, deleted);
    }

    private User createUser(UserRole role, boolean deleted) {
        return User.restore(
                1L, role, "student@test.com", "ENCODED_PW",
                "Student", "student01", "010-1234-5678",
                AuthProvider.LOCAL, null,
                true, false, null, null,
                LocalDateTime.now(), LocalDateTime.now(),
                deleted ? LocalDateTime.now() : null
        );
    }
}
