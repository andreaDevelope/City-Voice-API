package it.cityvoice.api.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

public class JwtCookieUtils {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final int COOKIE_MAX_AGE = 86400;
    private static final String COOKIE_SAME_SITE = "Lax";

    private JwtCookieUtils() {
        // Utility class, no instantiation
    }

    public static ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .sameSite(COOKIE_SAME_SITE)
                .build();
    }

    public static String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie != null && ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static String getAccessTokenCookieName() {
        return ACCESS_TOKEN_COOKIE_NAME;
    }
}
