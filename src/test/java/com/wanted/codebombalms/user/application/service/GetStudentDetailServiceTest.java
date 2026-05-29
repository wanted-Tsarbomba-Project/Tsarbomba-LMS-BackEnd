package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.application.query.StudentDetail;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStudentDetailService 단위 테스트")
class GetStudentDetailServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private GetStudentDetailService getStudentDetailService;

    @Test
    @DisplayName("userId로 학생 상세 정보를 조회한다.")
    void 학생_상세_조회_성공() {
        // given
        User user = User.restore(
                5L, UserRole.STUDENT, "student5@test.com", "ENCODED_PW",
                "김학생", "학생05", "010-5555-5555",
                AuthProvider.LOCAL, null,
                true, false, null, null,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
        given(userRepository.findByUserId(5L)).willReturn(Optional.of(user));

        // when
        StudentDetail result = getStudentDetailService.getStudentDetail(5L);

        // then
        assertEquals(5L, result.userId());
        assertEquals("student5@test.com", result.email());
        assertFalse(result.isLocked());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 NotFoundException(USER_NOT_FOUND)을 던진다.")
    void 회원_없음_예외() {
        given(userRepository.findByUserId(99L)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> getStudentDetailService.getStudentDetail(99L)
        );
        assertEquals(UserErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }
}
