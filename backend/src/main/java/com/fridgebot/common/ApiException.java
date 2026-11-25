package com.fridgebot.common;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * 업무 규칙 위반을 RFC 9457 ProblemDetail로 돌려주기 위한 예외.
 * 프론트가 문구 대신 {@code code}로 분기할 수 있게 고정 코드를 함께 담는다.
 */
public class ApiException extends ErrorResponseException {

	private final String code;

	public ApiException(HttpStatus status, String code, String detail) {
		super(status, problem(status, code, detail), null);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	private static ProblemDetail problem(HttpStatus status, String code, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(URI.create("urn:fridge-bot:error:" + code.toLowerCase().replace('_', '-')));
		problem.setProperty("code", code);
		return problem;
	}

	public static ApiException badRequest(String code, String detail) {
		return new ApiException(HttpStatus.BAD_REQUEST, code, detail);
	}

	public static ApiException unauthorized(String code, String detail) {
		return new ApiException(HttpStatus.UNAUTHORIZED, code, detail);
	}

	public static ApiException forbidden(String code, String detail) {
		return new ApiException(HttpStatus.FORBIDDEN, code, detail);
	}

	public static ApiException notFound(String code, String detail) {
		return new ApiException(HttpStatus.NOT_FOUND, code, detail);
	}

	public static ApiException conflict(String code, String detail) {
		return new ApiException(HttpStatus.CONFLICT, code, detail);
	}

}
