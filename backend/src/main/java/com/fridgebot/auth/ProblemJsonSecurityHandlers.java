package com.fridgebot.auth;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/** 인증·인가 실패도 API의 다른 오류와 같은 ProblemDetail 형식으로 돌려준다. */
class ProblemJsonSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
		write(response, 401, "Unauthorized", "UNAUTHORIZED", "로그인이 필요합니다.");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		write(response, 403, "Forbidden", "FORBIDDEN", "권한이 없습니다.");
	}

	private static void write(HttpServletResponse response, int status, String title, String code, String detail)
			throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter()
			.write("{\"type\":\"urn:fridge-bot:error:%s\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\",\"code\":\"%s\"}"
				.formatted(code.toLowerCase(), title, status, detail, code));
	}

}
