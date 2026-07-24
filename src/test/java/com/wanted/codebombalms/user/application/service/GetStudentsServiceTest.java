package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.user.application.query.StudentPageResult;
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserPage;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import com.wanted.codebombalms.user.infrastructure.metrics.UserMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStudentsService 단위 테스트")
class GetStudentsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMetrics userMetrics;

    @InjectMocks
    private GetStudentsService getStudentsService;

    @Test
    @DisplayName("keyword 없이 학생 목록을 조회하고 전체 페이지 수를 올림 계산한다. (총 25명, size 10 → 3페이지)")
    void 목록_조회_및_페이지_계산() {
        // given
        given(userRepository.findAllByRoleAndKeyword(UserRole.STUDENT, null, 0, 10))
                .willReturn(new UserPage(List.of(createUser(1L), createUser(2L)), 25L));

        // when
        StudentPageResult result = getStudentsService.getStudents(0, 10, null);

        // then
        assertEquals(2, result.content().size());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages()); // ceil(25/10)
        verify(userRepository).findAllByRoleAndKeyword(UserRole.STUDENT, null, 0, 10);
        verify(userMetrics).recordStudentListQuery(anyLong()); // 쿼리 구간 계측이 실제로 호출되는지 고정
    }

    @Test
    @DisplayName("keyword를 가공 없이 리포지토리로 전달한다. (공백 정규화는 어댑터 책임)")
    void 키워드_리포지토리_위임() {
        // given
        given(userRepository.findAllByRoleAndKeyword(UserRole.STUDENT, "  김학생 ", 0, 10))
                .willReturn(new UserPage(List.of(createUser(1L)), 1L));

        // when
        StudentPageResult result = getStudentsService.getStudents(0, 10, "  김학생 ");

        // then
        assertEquals(1, result.content().size());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
        verify(userRepository).findAllByRoleAndKeyword(UserRole.STUDENT, "  김학생 ", 0, 10);
    }

    @Test
    @DisplayName("size가 0이면 totalPages를 0으로 방어한다. (컨트롤러 @Min(1) 뒤의 2차 방어선)")
    void size_0_방어() {
        // given
        given(userRepository.findAllByRoleAndKeyword(UserRole.STUDENT, null, 0, 0))
                .willReturn(new UserPage(List.of(), 10L));

        // when
        StudentPageResult result = getStudentsService.getStudents(0, 0, null);

        // then
        assertEquals(0, result.totalPages());
        assertTrue(result.content().isEmpty());
    }

    @Test
    @DisplayName("조회 중 예외가 발생하면 그대로 전파하고, 메트릭은 기록하지 않는다.")
    void 조회_예외_전파() {
        // given
        given(userRepository.findAllByRoleAndKeyword(UserRole.STUDENT, null, 0, 10))
                .willThrow(new RuntimeException("DB unavailable"));

        // when & then
        assertThrows(RuntimeException.class, () -> getStudentsService.getStudents(0, 10, null));
        // 실패 시엔 쿼리 구간 계측을 남기지 않는다(성공 케이스만 집계 — finally 미사용 동작 고정)
        verify(userMetrics, never()).recordStudentListQuery(anyLong());
    }

    // ===== 테스트 헬퍼 =====

    private User createUser(Long userId) {
        return User.restore(
                userId, UserRole.STUDENT, "student" + userId + "@test.com", "ENCODED_PW",
                "김학생", "학생" + userId, "010-1234-5678",
                AuthProvider.LOCAL, null,
                true, false, null, null,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
    }
}