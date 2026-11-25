package com.fridgebot.auth;

import java.util.Optional;

import com.fridgebot.common.SecureRandomStrings;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 리프레시 토큰은 불투명한 임의 문자열이고 Redis에 저장한다.
 * <ul>
 * <li>토큰 형식: {@code {userId}.{random}}</li>
 * <li>Redis 키: {@code rt:{userId}:{sha256(random)}} — 원문은 저장하지 않는다</li>
 * <li>사용할 때마다 지우고 새로 발급(회전)한다. 이미 쓴 토큰은 다시 쓸 수 없다.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

	private final StringRedisTemplate redis;

	private final AuthProperties properties;

	public String issue(Long userId) {
		String secret = SecureRandomStrings.urlSafe(32);
		redis.opsForValue().set(key(userId, secret), "1", properties.refreshTokenTtl());
		return userId + "." + secret;
	}

	/** 토큰이 유효하면 지우고 사용자 ID를 돌려준다. 지우기와 확인을 DEL 한 번으로 처리해 동시 사용을 막는다. */
	public Optional<Long> consume(String token) {
		return parse(token).filter(t -> Boolean.TRUE.equals(redis.delete(key(t.userId(), t.secret()))))
			.map(ParsedToken::userId);
	}

	public void revoke(String token) {
		parse(token).ifPresent(t -> redis.delete(key(t.userId(), t.secret())));
	}

	private static String key(Long userId, String secret) {
		return "rt:" + userId + ":" + SecureRandomStrings.sha256Hex(secret);
	}

	private static Optional<ParsedToken> parse(String token) {
		if (token == null) {
			return Optional.empty();
		}
		int dot = token.indexOf('.');
		if (dot <= 0 || dot == token.length() - 1) {
			return Optional.empty();
		}
		try {
			return Optional.of(new ParsedToken(Long.valueOf(token.substring(0, dot)), token.substring(dot + 1)));
		}
		catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

	private record ParsedToken(Long userId, String secret) {
	}

}
