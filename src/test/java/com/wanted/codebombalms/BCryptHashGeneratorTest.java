package com.wanted.codebombalms;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 시연용 더미 계정 비밀번호 BCrypt 해시 생성 유틸.
 * 한 번 돌려서 콘솔에 출력된 해시를 demo/user_dummy_data.sql 에 붙여넣는다.
 *
 * 실행: ./gradlew test --tests "com.wanted.codebombalms.BCryptHashGeneratorTest"
 */
class BCryptHashGeneratorTest {

    @Test
    void printHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String[] passwords = {"password", "Password1!", "Demo1234!"};

        System.out.println("=== BCrypt Hash (Spring Security 표준) ===");
        for (String pw : passwords) {
            String hash = encoder.encode(pw);
            boolean matches = encoder.matches(pw, hash);
            System.out.println("평문='" + pw + "'  →  " + hash + "  (검증=" + matches + ")");
        }
        System.out.println("==========================================");
    }
}
