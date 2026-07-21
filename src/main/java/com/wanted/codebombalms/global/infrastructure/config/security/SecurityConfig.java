package com.wanted.codebombalms.global.infrastructure.config.security;

import com.wanted.codebombalms.auth.domain.repository.AuthSessionRepository;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtAuthenticationFilter;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthSessionRepository authSessionRepository;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    // 허용 오리진(콤마 구분). 배포는 env(CORS_ALLOWED_ORIGINS)로 주입, 기본은 로컬 FE
    @Value("${cors.allowed-origins:http://localhost:3001}")
    private List<String> allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // SSE(servlet async) 완료 시 Tomcat 이 ASYNC 로 필터체인을 재진입하는데,
                        // OncePerRequestFilter 인 JwtAuthenticationFilter 는 ASYNC 에서 재실행되지 않아
                        // SecurityContext 가 비고 → AuthorizationFilter 가 Access Denied → 응답 이미 커밋됨
                        // → ERR_INCOMPLETE_CHUNKED_ENCODING. 최초 REQUEST 는 이미 인가되므로 ASYNC/ERROR 는 허용한다.
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // 모니터링 (Prometheus 스크레이핑)
                        .requestMatchers("/actuator/**").permitAll()
                        // FastAPI opschat 도구 전용 내부 API — JWT 미사용.
                        // 인증은 InternalOpsController 의 X-Internal-Token 검증(fail-closed)이 담당하고,
                        // 배포에서는 SG 로 파이썬 박스만 접근 허용 (이중 방어)
                        .requestMatchers("/internal/ops/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        // 인증 불필요
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/find-email").permitAll()
                        // 강의/코스 브라우징
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/course-categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures/**").permitAll()
                        // 운영/관리자 전용 (권한 경계 확정 전까지 둘 다 허용)
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "OPERATOR", "MASTER")
                        // 그 외 모두 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, authSessionRepository),
                        UsernamePasswordAuthenticationFilter.class
                ).exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
