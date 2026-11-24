package com.fridgebot.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fridgebot.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * 소셜 로그인 성공 이후의 흐름: 1회용 코드 → 토큰 교환 → API 호출 → 리프레시 회전 → 로그아웃.
 * (구글·카카오 서버와의 OAuth2 교환은 Spring Security가 담당하므로 여기서는 성공 핸들러가 만드는 코드부터 검증)
 */
class AuthFlowIntegrationTest extends IntegrationTestSupport {

	@Autowired
	LoginCodeStore loginCodeStore;

	@Test
	void socialLoginStartsWithRedirectToProvider() {
		assertThat(redirectLocation("/oauth2/authorization/google")).startsWith("https://accounts.google.com/");
		assertThat(redirectLocation("/oauth2/authorization/kakao"))
			.startsWith("https://kauth.kakao.com/oauth/authorize");
	}

	private String redirectLocation(String uri) {
		MvcTestResult result = mvc.get().uri(uri).exchange();
		assertThat(result).hasStatus(HttpStatus.FOUND);
		return result.getResponse().getHeader(HttpHeaders.LOCATION);
	}

	@Test
	void loginCodeExchangesForTokensOnlyOnce() {
		Long userId = newUser("민지");
		String code = loginCodeStore.issue(userId);

		MvcTestResult first = exchange(code);
		assertThat(first).hasStatusOk();
		assertThat(first).bodyJson().extractingPath("$.tokenType").isEqualTo("Bearer");
		assertThat(refreshCookie(first).isHttpOnly()).isTrue();
		assertThat(refreshCookie(first).getPath()).isEqualTo("/api/auth");

		assertThat(exchange(code)).hasStatus(HttpStatus.UNAUTHORIZED)
			.bodyJson().extractingPath("$.code").isEqualTo("INVALID_LOGIN_CODE");
	}

	@Test
	void accessTokenAuthenticatesApiCalls() {
		Long userId = newUser("민지");
		String accessToken = JsonPath.read(body(exchange(loginCodeStore.issue(userId))), "$.accessToken");

		assertThat(mvc.get().uri("/api/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.hasStatusOk()
			.bodyJson()
			.extractingPath("$.nickname").isEqualTo("민지");
	}

	@Test
	void missingOrTamperedTokenIsRejectedAsProblemJson() {
		assertThat(mvc.get().uri("/api/me")).hasStatus(HttpStatus.UNAUTHORIZED)
			.hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyJson().extractingPath("$.code").isEqualTo("UNAUTHORIZED");

		String tampered = bearer(newUser("민지")) + "x";
		assertThat(mvc.get().uri("/api/me").header(HttpHeaders.AUTHORIZATION, tampered))
			.hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void refreshRotatesTokenAndRejectsReuse() {
		Long userId = newUser("민지");
		Cookie original = refreshCookie(exchange(loginCodeStore.issue(userId)));

		MvcTestResult refreshed = refresh(original);
		assertThat(refreshed).hasStatusOk();
		Cookie rotated = refreshCookie(refreshed);
		assertThat(rotated.getValue()).isNotEqualTo(original.getValue());

		// 이미 쓴 리프레시 토큰은 다시 쓸 수 없다
		assertThat(refresh(original)).hasStatus(HttpStatus.UNAUTHORIZED)
			.bodyJson().extractingPath("$.code").isEqualTo("INVALID_REFRESH_TOKEN");
		assertThat(refresh(rotated)).hasStatusOk();
	}

	@Test
	void logoutRevokesRefreshToken() {
		Long userId = newUser("민지");
		Cookie cookie = refreshCookie(exchange(loginCodeStore.issue(userId)));

		MvcTestResult logout = mvc.post().uri("/api/auth/logout").cookie(cookie).exchange();
		assertThat(logout).hasStatus(HttpStatus.NO_CONTENT);
		assertThat(refreshCookie(logout).getMaxAge()).isZero();

		assertThat(refresh(cookie)).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void forgedRefreshTokenIsRejected() {
		Long userId = newUser("민지");

		assertThat(refresh(new Cookie(AuthController.REFRESH_COOKIE, userId + ".forged"))).hasStatus(HttpStatus.UNAUTHORIZED);
		assertThat(refresh(new Cookie(AuthController.REFRESH_COOKIE, "garbage"))).hasStatus(HttpStatus.UNAUTHORIZED);
		assertThat(mvc.post().uri("/api/auth/refresh")).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	private MvcTestResult exchange(String code) {
		return mvc.post()
			.uri("/api/auth/token")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"code\":\"" + code + "\"}")
			.exchange();
	}

	private MvcTestResult refresh(Cookie cookie) {
		return mvc.post().uri("/api/auth/refresh").cookie(cookie).exchange();
	}

	private static Cookie refreshCookie(MvcTestResult result) {
		Cookie cookie = result.getResponse().getCookie(AuthController.REFRESH_COOKIE);
		assertThat(cookie).as("refresh cookie").isNotNull();
		return cookie;
	}

	private static String body(MvcTestResult result) {
		try {
			return result.getResponse().getContentAsString();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

}
