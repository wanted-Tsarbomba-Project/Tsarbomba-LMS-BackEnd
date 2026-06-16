package com.wanted.codebombalms.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User 도메인 — 탈퇴(softDelete) 묘비 처리")
class UserTest {

    private User activeUser(Long id, String email, String nickname) {
        return User.restore(
                id, UserRole.STUDENT, email, "ENCODED_PW",
                "김학생", nickname, "010-1234-5678",
                AuthProvider.LOCAL, null,
                true, false, null, null,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
    }

    @Test
    @DisplayName("탈퇴 시 deleted_at 기록 + 닉네임/이메일 슬롯을 묘비화해 해방한다.")
    void 탈퇴_묘비화() {
        User user = activeUser(42L, "user@test.com", "학생01");

        user.softDelete();

        assertTrue(user.isDeleted());
        assertEquals("탈퇴회원_42", user.getNickname());
        assertEquals("user@test.com#deleted#42", user.getEmail());
    }

    @Test
    @DisplayName("탈퇴 이메일은 원본 슬롯을 비워 재사용 가능해지고, 원본은 #deleted# 앞부분으로 복원된다.")
    void 이메일_슬롯_해방_및_복원() {
        User user = activeUser(7L, "reuse@test.com", "닉네임");

        user.softDelete();

        assertNotEquals("reuse@test.com", user.getEmail());
        String restored = user.getEmail().substring(0, user.getEmail().lastIndexOf("#deleted#"));
        assertEquals("reuse@test.com", restored, "원본 이메일이 복원되어야 한다");
    }

    @Test
    @DisplayName("이미 탈퇴한 회원을 다시 탈퇴 처리해도 묘비가 중복으로 덧씌워지지 않는다(멱등).")
    void 이중_묘비화_방지() {
        User user = activeUser(5L, "once@test.com", "한번만");

        user.softDelete();
        String email = user.getEmail();
        String nickname = user.getNickname();
        user.softDelete(); // 두 번째 호출

        assertEquals(email, user.getEmail());
        assertEquals(nickname, user.getNickname());
        assertFalse(user.getEmail().contains("#deleted##deleted#"), "마커 중복 금지");
    }

    @Test
    @DisplayName("이메일이 길어 접미사 붙이면 100자를 넘는 경우, 100자 이내로 자르되 userId 접미사로 고유성을 유지한다.")
    void 이메일_길이_가드() {
        User user = activeUser(123L, "a".repeat(95), "롱이메일"); // 95 + "#deleted#123"(12) = 107 > 100

        user.softDelete();

        assertTrue(user.getEmail().length() <= 100, "묘비 이메일은 100자를 넘으면 안 된다");
        assertTrue(user.getEmail().endsWith("#deleted#123"), "userId 접미사로 고유성 유지");
    }
}
