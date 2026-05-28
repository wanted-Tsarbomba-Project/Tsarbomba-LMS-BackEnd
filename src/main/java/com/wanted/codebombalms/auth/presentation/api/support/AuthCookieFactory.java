package com.wanted.codebombalms.auth.presentation.api.support;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieFactory {

    private final boolean secure;

    public AuthCookieFactory(@Value("${app.cookie.secure}") boolean secure) {
        this.secure = secure;
    }

    public Cookie create(String name, String value, int maxAgeSeconds) {
        return build(name, value, maxAgeSeconds);
    }

    public Cookie expired(String name) {
        return build(name, "", 0);
    }

    private Cookie build(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
