package com.fridgebot.auth;

import com.fridgebot.auth.AccessTokenService.AccessToken;
import com.fridgebot.common.ApiException;
import com.fridgebot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final LoginCodeStore loginCodeStore;

	private final RefreshTokenStore refreshTokenStore;

	private final AccessTokenService accessTokenService;

	private final UserRepository userRepository;

	public Tokens exchangeLoginCode(String code) {
		Long userId = loginCodeStore.consume(code)
			.orElseThrow(() -> ApiException.unauthorized("INVALID_LOGIN_CODE", "로그인 코드가 없거나 만료되었습니다."));
		return issue(userId);
	}

	public Tokens refresh(String refreshToken) {
		Long userId = refreshTokenStore.consume(refreshToken)
			.orElseThrow(() -> ApiException.unauthorized("INVALID_REFRESH_TOKEN", "다시 로그인해 주세요."));
		return issue(userId);
	}

	public void logout(String refreshToken) {
		refreshTokenStore.revoke(refreshToken);
	}

	private Tokens issue(Long userId) {
		// 코드·토큰을 받은 사이 탈퇴한 사용자에게는 새 토큰을 주지 않는다.
		if (!userRepository.existsById(userId)) {
			throw ApiException.unauthorized("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
		}
		return new Tokens(accessTokenService.issue(userId), refreshTokenStore.issue(userId));
	}

	public record Tokens(AccessToken accessToken, String refreshToken) {
	}

}
