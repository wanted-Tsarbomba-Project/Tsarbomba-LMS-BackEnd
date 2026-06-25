package com.wanted.codebombalms.auth.presentation.api.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeviceFingerprintResolver {

    private static final String DEVICE_ID_COOKIE = "deviceId";
    private static final int DEVICE_ID_MAX_AGE = 60 * 60 * 24 * 365; // 1년 (영속)

    private final AuthCookieFactory authCookieFactory;

    /**
     * 요청의 deviceId 쿠키로 기기 지문(SHA-256 64자)을 반환한다.
     * 쿠키가 없으면 새 deviceId 를 발급해 응답 쿠키로 심는다. (식별용 — 인증은 OTP가 담당)
     */
    public String resolve(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = readDeviceId(request);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            response.addCookie(authCookieFactory.create(DEVICE_ID_COOKIE, deviceId, DEVICE_ID_MAX_AGE));
        }
        return sha256(deviceId);
    }

    private String readDeviceId(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> DEVICE_ID_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash); // 소문자 hex 64자 → device_fp VARCHAR(64)
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
