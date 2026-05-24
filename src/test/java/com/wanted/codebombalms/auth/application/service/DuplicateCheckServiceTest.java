package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DuplicateCheckService 단위 테스트")
class DuplicateCheckServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DuplicateCheckService duplicateCheckService;

    @Test
    @DisplayName("사용 가능한 이메일이면 true를 반환한다.")
    void 이메일_사용_가능() {
        // given
        String email = "new@example.com";
        given(userRepository.existsByEmail(email)).willReturn(false);

        // when
        boolean result = duplicateCheckService.isEmailAvailable(email);

        // then
        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 false를 반환한다.")
    void 이메일_중복() {
        // given
        String email = "existing@example.com";
        given(userRepository.existsByEmail(email)).willReturn(true);

        // when
        boolean result = duplicateCheckService.isEmailAvailable(email);

        // then
        assertFalse(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("사용 가능한 닉네임이면 true를 반환한다.")
    void 닉네임_사용_가능() {
        // given
        String nickname = "새닉네임";
        given(userRepository.existsByNickname(nickname)).willReturn(false);

        // when
        boolean result = duplicateCheckService.isNicknameAvailable(nickname);

        // then
        assertTrue(result);
        verify(userRepository).existsByNickname(nickname);
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임이면 false를 반환한다.")
    void 닉네임_중복() {
        // given
        String nickname = "기존닉네임";
        given(userRepository.existsByNickname(nickname)).willReturn(true);

        // when
        boolean result = duplicateCheckService.isNicknameAvailable(nickname);

        // then
        assertFalse(result);
        verify(userRepository).existsByNickname(nickname);
    }
}