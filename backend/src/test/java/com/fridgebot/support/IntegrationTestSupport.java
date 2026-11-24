package com.fridgebot.support;

import java.util.UUID;

import com.fridgebot.auth.AccessTokenService;
import com.fridgebot.user.AuthProvider;
import com.fridgebot.user.OAuthProfile;
import com.fridgebot.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * 테스트마다 새 사용자를 만들어 데이터가 서로 겹치지 않게 한다 (테이블을 비우지 않아도 됨).
 */
@IntegrationTest
public abstract class IntegrationTestSupport {

	@Autowired
	protected MockMvcTester mvc;

	@Autowired
	protected UserService userService;

	@Autowired
	protected AccessTokenService accessTokenService;

	protected Long newUser(String nickname) {
		return userService.upsert(new OAuthProfile(AuthProvider.GOOGLE, UUID.randomUUID().toString(), nickname, null))
			.getId();
	}

	protected String bearer(Long userId) {
		return "Bearer " + accessTokenService.issue(userId).value();
	}

}
