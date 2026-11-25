package com.fridgebot.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class OAuthProfileTest {

	@Test
	void mapsGoogleAttributes() {
		OAuthProfile profile = OAuthProfile.from("google",
				Map.of("sub", "g-123", "name", "냉장고", "email", "a@example.com"));

		assertThat(profile).isEqualTo(new OAuthProfile(AuthProvider.GOOGLE, "g-123", "냉장고", "a@example.com"));
	}

	@Test
	void mapsKakaoNestedAttributes() {
		OAuthProfile profile = OAuthProfile.from("kakao",
				Map.of("id", 42L, "kakao_account", Map.of("profile", Map.of("nickname", "카카오"))));

		assertThat(profile).isEqualTo(new OAuthProfile(AuthProvider.KAKAO, "42", "카카오", null));
	}

	@Test
	void kakaoWithoutAccountInfoStillHasId() {
		OAuthProfile profile = OAuthProfile.from("kakao", Map.of("id", 7));

		assertThat(profile.providerId()).isEqualTo("7");
		assertThat(profile.nickname()).isNull();
	}

}
