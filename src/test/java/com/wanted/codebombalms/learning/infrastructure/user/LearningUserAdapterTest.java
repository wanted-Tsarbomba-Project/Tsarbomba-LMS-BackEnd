package com.wanted.codebombalms.learning.infrastructure.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LearningUserAdapterTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LearningUserAdapter learningUserAdapter;

    @Test
    void findUserName_returnsNicknameBeforeName() {
        Long userId = 10L;
        given(userRepository.findByUserId(userId))
                .willReturn(Optional.of(user(userId, "김학생", "user01")));

        String result = learningUserAdapter.findUserName(userId);

        assertEquals("user01", result);
    }

    @Test
    void findUserNames_returnsNicknamesBeforeNames() {
        List<Long> userIds = List.of(10L, 11L);
        given(userRepository.findByUserIds(userIds))
                .willReturn(List.of(
                        user(10L, "김학생", "user01"),
                        user(11L, "이학생", "user02")
                ));

        Map<Long, String> result = learningUserAdapter.findUserNames(userIds);

        assertEquals("user01", result.get(10L));
        assertEquals("user02", result.get(11L));
    }

    private User user(Long userId, String name, String nickname) {
        return User.restore(
                userId,
                UserRole.STUDENT,
                "student" + userId + "@test.com",
                "password",
                name,
                nickname,
                "010-0000-0000",
                AuthProvider.LOCAL,
                null,
                true,
                false,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }
}
