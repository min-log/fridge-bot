package com.fridgebot.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param jwtSecret Base64로 인코딩한 HMAC 키 (32바이트 이상). 환경변수 JWT_SECRET으로만 받는다.
 * @param frontendUrl 로그인 후 돌아갈 프론트 주소
 */
@ConfigurationProperties("app.auth")
public record AuthProperties(
		String jwtSecret,
		String frontendUrl,
		Duration accessTokenTtl,
		Duration refreshTokenTtl,
		Duration loginCodeTtl) {

	public AuthProperties {
		if (jwtSecret == null || jwtSecret.isBlank()) {
			throw new IllegalStateException("JWT_SECRET 환경변수가 필요합니다.");
		}
		if (frontendUrl == null || frontendUrl.isBlank()) {
			throw new IllegalStateException("APP_FRONTEND_URL 환경변수가 필요합니다.");
		}
		accessTokenTtl = accessTokenTtl == null ? Duration.ofMinutes(30) : accessTokenTtl;
		refreshTokenTtl = refreshTokenTtl == null ? Duration.ofDays(14) : refreshTokenTtl;
		loginCodeTtl = loginCodeTtl == null ? Duration.ofSeconds(60) : loginCodeTtl;
	}

}
