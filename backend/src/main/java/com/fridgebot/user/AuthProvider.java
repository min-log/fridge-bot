package com.fridgebot.user;

import java.util.Locale;

public enum AuthProvider {

	GOOGLE, KAKAO;

	/** Spring Security의 registrationId(google, kakao)를 enum으로 바꾼다. */
	public static AuthProvider fromRegistrationId(String registrationId) {
		return valueOf(registrationId.toUpperCase(Locale.ROOT));
	}

}
