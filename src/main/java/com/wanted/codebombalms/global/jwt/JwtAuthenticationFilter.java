package com.wanted.codebombalms.global.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import com.wanted.codebombalms.global.error.exception.UnauthorizedException;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

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
                String role = claims.get("role", String.class);

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