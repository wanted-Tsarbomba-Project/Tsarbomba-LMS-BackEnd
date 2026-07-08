package com.wanted.codebombalms.global.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 클라이언트 IP 해석 단일 지점.
 * 현재 BE 직결 노출 환경이라 위조 가능한 X-Forwarded-For 를 신뢰하지 않고 실제 접속 IP 를 사용한다.
 * 신뢰 프록시(RemoteIpValve 등) 도입으로 IP 정책이 바뀌면 이 메서드 한 곳만 수정한다. (M-3)
 */
public final class ClientIpResolver {

    private ClientIpResolver() {}

    public static String resolve(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
