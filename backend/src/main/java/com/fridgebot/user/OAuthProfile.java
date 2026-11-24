package com.fridgebot.user;

import java.util.Map;

/** 소셜 로그인 제공자마다 다른 사용자 정보 응답을 하나의 형태로 맞춘다. */
public record OAuthProfile(AuthProvider provider, String providerId, String nickname, String email) {

	public static OAuthProfile from(String registrationId, Map<String, Object> attributes) {
		AuthProvider provider = AuthProvider.fromRegistrationId(registrationId);
		return switch (provider) {
			case GOOGLE -> new OAuthProfile(provider, string(attributes.get("sub")),
					string(attributes.get("name")), string(attributes.get("email")));
			case KAKAO -> {
				Map<String, Object> account = map(attributes.get("kakao_account"));
				Map<String, Object> profile = map(account.get("profile"));
				yield new OAuthProfile(provider, string(attributes.get("id")),
						string(profile.get("nickname")), string(account.get("email")));
			}
		};
	}

	private static String string(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> map(Object value) {
		return value instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
	}

}
