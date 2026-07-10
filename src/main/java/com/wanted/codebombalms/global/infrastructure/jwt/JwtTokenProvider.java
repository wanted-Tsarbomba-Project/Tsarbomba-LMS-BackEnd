package com.wanted.codebombalms.global.infrastructure.jwt;

import com.wanted.codebombalms.user.domain.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final Key key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /** sid(세션ID) 포함 AT 발급 — 단일세션 + 실시간 잠금 검증용. */
    public String generateAccessToken(Long userId, String nickname, UserRole role, String sid) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("typ", TYPE_ACCESS)
                .claim("nickname", nickname)
                .claim("role", role.name())
                .claim("sid", sid)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(key)
                .compact();
    }

    /** AT claims 에서 sid 추출 — 필터가 Redis 세션과 대조. */
    public String getSid(Claims claims) {
        return claims.get("sid", String.class);
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("typ", TYPE_REFRESH)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ===== 토큰 검증 (Access / Refresh 분리) =====

    public void validateAccessToken(String token) {
        try {
            Claims claims = getClaims(token);
            // refresh token을 accessToken 쿠키에 넣어 우회하는 것을 차단
            if (!TYPE_ACCESS.equals(claims.get("typ", String.class))
                    || claims.get("role", String.class) == null) {
                throw new UnauthorizedException(AuthErrorCode.AUTH_TOKEN_INVALID);
            }
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    public void validateRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            if (!TYPE_REFRESH.equals(claims.get("typ", String.class))) {
                throw new UnauthorizedException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID);
            }
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }
    }

    public long getAccessExpiration() {return accessExpiration;}

    public long getRefreshExpiration() {return refreshExpiration;}
}
