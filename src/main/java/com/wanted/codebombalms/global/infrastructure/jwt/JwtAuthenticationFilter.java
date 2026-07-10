package com.wanted.codebombalms.global.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import com.wanted.codebombalms.auth.domain.repository.AuthSessionRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHENTICATED_USER_ID_ATTRIBUTE = "authenticatedUserId";
    public static final String AUTHENTICATED_ROLE_ATTRIBUTE = "authenticatedRole";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthSessionRepository authSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 쿠키에서 토큰 꺼내기
        Cookie cookie = WebUtils.getCookie(request, "accessToken");

        if (cookie != null) {
            String token = cookie.getValue();

            try {
                // 2. 토큰 유효성 검사 (실패 시 예외 throw)
                jwtTokenProvider.validateAccessToken(token);

                // 3. claims에서 userId, role 꺼내기
                Claims claims = jwtTokenProvider.getClaims(token);
                Long userId = Long.parseLong(claims.getSubject());

                // 3-1. 세션(sid) 검증 — 단일세션(P0-2) + 실시간 잠금/탈퇴 반영(P0-1)
                //      Redis 장애 시 fail-closed(안전측): 검증 불가 → 미인증 처리(401)
                String sid = jwtTokenProvider.getSid(claims);
                boolean sessionValid;
                try {
                    sessionValid = sid != null
                            && authSessionRepository.findSid(userId).map(sid::equals).orElse(false);
                } catch (RuntimeException redisDown) {
                    sessionValid = false;
                    log.warn("세션(sid) 검증 실패 — Redis 접근 오류로 fail-closed 처리. userId={}", userId, redisDown);
                }
                if (!sessionValid) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                String role = claims.get("role", String.class);
                request.setAttribute(AUTHENTICATED_USER_ID_ATTRIBUTE, userId);
                request.setAttribute(AUTHENTICATED_ROLE_ATTRIBUTE, role);

                // 4. SecurityContextHolder에 저장
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (UnauthorizedException e) {
                // 토큰이 유효하지 않으면 SecurityContext 비우고 다음 필터로 진행
                // (실제 401 응답은 SecurityFilterChain 의 ExceptionTranslationFilter 가 처리)
                SecurityContextHolder.clearContext();
            }
        }

        // 5. 다음 필터로
        filterChain.doFilter(request, response);
    }
}
