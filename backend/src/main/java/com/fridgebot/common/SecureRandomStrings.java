package com.fridgebot.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import java.nio.charset.StandardCharsets;

/** 토큰·코드 생성과 해시에 쓰는 도구. */
public final class SecureRandomStrings {

	private static final SecureRandom RANDOM = new SecureRandom();

	private SecureRandomStrings() {
	}

	/** URL에 그대로 쓸 수 있는 임의 문자열 (bytes 바이트 엔트로피). */
	public static String urlSafe(int bytes) {
		byte[] buf = new byte[bytes];
		RANDOM.nextBytes(buf);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
	}

	/** 주어진 문자 집합에서 length 글자를 고른다. */
	public static String fromAlphabet(String alphabet, int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
		}
		return sb.toString();
	}

	public static String sha256Hex(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
