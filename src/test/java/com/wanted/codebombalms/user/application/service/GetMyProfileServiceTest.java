package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.application.query.MyProfileResult;
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
@DisplayName("GetMyProfileService 단위 테스트")
class GetMyProfileServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private GetMyProfileService getMyProfileService;

    @Test
    @DisplayName("본인 userId로 프로필을 조회하면 회원 정보를 반환한다.")
    void 내정보_조회_성공() {
        // given
        User user = User.restore(
                1L, UserRole.STUDENT, "me@test.com", "ENCODED_PW",
                "김학생", "학생01", "010-1234-5678",
                AuthProvider.LOCAL, null,
                true, false, null, null,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(user));

        // when
        MyProfileResult result = getMyProfileService.getMyProfile(1L);

        // then
        assertEquals("me@test.com", result.email());
        assertEquals("학생01", result.nickname());
        assertEquals(UserRole.STUDENT, result.role());
        assertTrue(result.emailVerified());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 NotFoundException(USER_NOT_FOUND)을 던진다.")
    void 회원_없음_예외() {
        given(userRepository.findByUserId(99L)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> getMyProfileService.getMyProfile(99L)
        );
        assertEquals(UserErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }
}
