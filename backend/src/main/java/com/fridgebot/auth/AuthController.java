package com.fridgebot.auth;

import java.time.Duration;

import com.fridgebot.auth.AuthService.Tokens;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 액세스 토큰은 응답 본문으로(프론트는 메모리에만 보관), 리프레시 토큰은 HttpOnly 쿠키로 준다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	static final String REFRESH_COOKIE = "refresh_token";

	private final AuthService authService;

	private final AuthProperties properties;

	/** 소셜 로그인 후 받은 1회용 코드를 토큰으로 교환한다. */
	@PostMapping("/token")
	public ResponseEntity<TokenResponse> token(@Valid @RequestBody LoginCodeRequest request) {
		return respond(authService.exchangeLoginCode(request.code()));
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refresh(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		return respond(authService.refresh(refreshToken));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		authService.logout(refreshToken);
		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, refreshCookie("", Duration.ZERO).toString())
			.build();
	}

	private ResponseEntity<TokenResponse> respond(Tokens tokens) {
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE,
					refreshCookie(tokens.refreshToken(), properties.refreshTokenTtl()).toString())
			.body(new TokenResponse(tokens.accessToken().value(), "Bearer",
					tokens.accessToken().expiresInSeconds()));
	}

	private static ResponseCookie refreshCookie(String value, Duration maxAge) {
		return ResponseCookie.from(REFRESH_COOKIE, value)
			.httpOnly(true)
			.secure(true)
			.sameSite("Strict")
			.path("/api/auth")
			.maxAge(maxAge)
			.build();
	}

	public record LoginCodeRequest(@NotBlank String code) {
	}

	public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
	}

}
