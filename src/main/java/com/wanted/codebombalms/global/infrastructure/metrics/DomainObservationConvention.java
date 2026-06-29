package com.wanted.codebombalms.global.infrastructure.metrics;

import com.wanted.codebombalms.global.infrastructure.jwt.JwtAuthenticationFilter;
import io.micrometer.common.KeyValues;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * 모놀리식 단일 앱에서 {@code http.server.requests} 메트릭에
 * "요청을 처리한 컨트롤러의 패키지" 기준 {@code domain} 태그를 자동 부착한다.
 *
 * <p>도메인별 대시보드는 {@code http_server_requests_seconds_count{domain="chat"}} 처럼
 * 이 태그 하나로 필터한다. uri 경로는 도메인 경계로 못 쓴다(예: {@code /api/v1/users}를
 * user·course가 공유) — 그래서 패키지 기반으로 가른다.
 *
 * <p>Spring Boot 3 에서는 {@code WebMvcTagsContributor}(Boot 2)가 제거되어
 * Observation API({@link DefaultServerRequestObservationConvention})로 처리한다.
 * 이 빈이 존재하면 기본 컨벤션 대신 사용된다.
 */
@Component
public class DomainObservationConvention extends DefaultServerRequestObservationConvention {

    private static final String BASE_PACKAGE = "com.wanted.codebombalms.";
    private static final String UNKNOWN = "unknown";
    private static final String ANONYMOUS = "anonymous";

    @Override
    public KeyValues getLowCardinalityKeyValues(ServerRequestObservationContext context) {
        return super.getLowCardinalityKeyValues(context)
                .and("domain", resolveDomain(context))
                .and("role", resolveRole(context));
    }

    private String resolveRole(ServerRequestObservationContext context) {
        HttpServletRequest request = context.getCarrier();
        if (request == null) {
            return ANONYMOUS;
        }

        Object role = request.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE_ATTRIBUTE);
        return role == null ? ANONYMOUS : String.valueOf(role);
    }

    private String resolveDomain(ServerRequestObservationContext context) {
        HttpServletRequest request = context.getCarrier();
        if (request == null) {
            return UNKNOWN;
        }

        // 핸들러 매핑 이후 설정되는 매칭 컨트롤러. 정적 리소스/404 등은 HandlerMethod 가 아니다.
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return UNKNOWN;
        }

        String packageName = handlerMethod.getBeanType().getPackageName();
        if (!packageName.startsWith(BASE_PACKAGE)) {
            return UNKNOWN;
        }

        // com.wanted.codebombalms.<domain>... 에서 첫 세그먼트를 도메인으로.
        // admin.operation.* 같은 중첩은 첫 세그먼트 "admin" 하나로 묶는다.
        String rest = packageName.substring(BASE_PACKAGE.length());
        int dot = rest.indexOf('.');
        return dot > 0 ? rest.substring(0, dot) : rest;
    }
}
