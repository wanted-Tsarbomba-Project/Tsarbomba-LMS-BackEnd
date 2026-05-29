package com.wanted.codebombalms.global.infrastructure.jwt;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.user.domain.model.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    // 32바이트(256bit) Base64 시크릿 — HS256 최소 길이 충족
    private static final String SECRET = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";
    private static final long ACCESS_EXP = 3600_000L;     // 1시간
    private static final long REFRESH_EXP = 1209600_000L; // 2주

    private final JwtTokenProvider provider =
            new JwtTokenProvider(SECRET, ACCESS_EXP, REFRESH_EXP);

    @Test
    @DisplayName("정상 access token은 검증을 통과하고 typ/role claim을 담는다.")
    void access_token_정상() {
        String token = provider.generateAccessToken(1L, "길동이", UserRole.STUDENT);

        assertDoesNotThrow(() -> provider.validateAccessToken(token));

        Claims claims = provider.getClaims(token);
        assertEquals("access", claims.get("typ", String.class));
        assertEquals("STUDENT", claims.get("role", String.class));
        assertEquals("1", claims.getSubject());
    }

    @Test
    @DisplayName("정상 refresh token은 검증을 통과하고 typ=refresh를 담는다.")
    void refresh_token_정상() {
        String token = provider.generateRefreshToken(1L);

        assertDoesNotThrow(() -> provider.validateRefreshToken(token));
        assertEquals("refresh", provider.getClaims(token).get("typ", String.class));
    }

    @Test
    @DisplayName("refresh token을 access로 검증하면 AUTH_TOKEN_INVALID로 차단한다. (타입 혼용 우회 방지)")
    void refresh를_access로_쓰면_차단() {
        String refreshToken = provider.generateRefreshToken(1L);

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> provider.validateAccessToken(refreshToken)
        );
        assertEquals(AuthErrorCode.AUTH_TOKEN_INVALID, ex.getErrorCode());
    }

    @Test
    @DisplayName("access token을 refresh로 검증하면 AUTH_REFRESH_TOKEN_INVALID로 차단한다. (역방향 오용 방지)")
    void access를_refresh로_쓰면_차단() {
        String accessToken = provider.generateAccessToken(1L, "길동이", UserRole.STUDENT);

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> provider.validateRefreshToken(accessToken)
        );
        assertEquals(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID, ex.getErrorCode());
    }

    @Test
    @DisplayName("만료된 access token은 AUTH_TOKEN_EXPIRED를 던진다.")
    void 만료된_access_token() {
        JwtTokenProvider expiredProvider =
                new JwtTokenProvider(SECRET, -1000L, REFRESH_EXP); // 발급 즉시 만료
        String expired = expiredProvider.generateAccessToken(1L, "길동이", UserRole.STUDENT);

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> provider.validateAccessToken(expired)
        );
        assertEquals(AuthErrorCode.AUTH_TOKEN_EXPIRED, ex.getErrorCode());
    }

    @Test
    @DisplayName("위조/형식 오류 토큰은 AUTH_TOKEN_INVALID를 던진다.")
    void 위조_토큰() {
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> provider.validateAccessToken("not.a.valid.token")
        );
        assertEquals(AuthErrorCode.AUTH_TOKEN_INVALID, ex.getErrorCode());
    }
}
