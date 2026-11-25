package com.fridgebot.auth;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/** 소셜 로그인 결과를 프론트로 돌려보낸다. 성공 시 토큰 대신 1회용 코드만 URL에 싣는다. */
@Component
@RequiredArgsConstructor
public class OAuth2LoginHandlers implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

	private final LoginCodeStore loginCodeStore;

	private final AuthProperties properties;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		OAuth2User user = (OAuth2User) authentication.getPrincipal();
		Long userId = user.getAttribute(AppOAuth2UserService.APP_USER_ID);
		String code = loginCodeStore.issue(userId);
		invalidateSession(request);
		response.sendRedirect(UriComponentsBuilder.fromUriString(properties.frontendUrl())
			.path("/oauth/callback")
			.queryParam("code", code)
			.toUriString());
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		invalidateSession(request);
		response.sendRedirect(UriComponentsBuilder.fromUriString(properties.frontendUrl())
			.path("/login")
			.queryParam("error", "oauth")
			.toUriString());
	}

	/** 세션은 OAuth2 state 보관에만 쓰므로 로그인이 끝나면 바로 버린다. */
	private static void invalidateSession(HttpServletRequest request) {
		var session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

}
