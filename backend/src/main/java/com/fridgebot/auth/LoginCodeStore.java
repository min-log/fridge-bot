package com.fridgebot.auth;

import java.util.Optional;

import com.fridgebot.common.SecureRandomStrings;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 소셜 로그인 성공 후 프론트로 넘기는 1회용 코드.
 * 토큰을 리다이렉트 URL에 직접 싣지 않고(브라우저 기록·로그에 남음), 짧게 사는 코드를 POST로 교환하게 한다.
 */
@Component
@RequiredArgsConstructor
public class LoginCodeStore {

	private static final String PREFIX = "login:code:";

	private final StringRedisTemplate redis;

	private final AuthProperties properties;

	public String issue(Long userId) {
		String code = SecureRandomStrings.urlSafe(32);
		redis.opsForValue().set(PREFIX + code, String.valueOf(userId), properties.loginCodeTtl());
		return code;
	}

	/** 코드를 읽으면서 바로 지운다 (GETDEL). 두 번째 사용은 실패한다. */
	public Optional<Long> consume(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(redis.opsForValue().getAndDelete(PREFIX + code)).map(Long::valueOf);
	}

}
