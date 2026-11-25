package com.fridgebot.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fridgebot.user.OAuthProfile;
import com.fridgebot.user.User;
import com.fridgebot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppOAuth2UserService extends DefaultOAuth2UserService {

	static final String APP_USER_ID = "appUserId";

	private final UserService userService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(request);
		OAuthProfile profile = OAuthProfile.from(request.getClientRegistration().getRegistrationId(),
				oAuth2User.getAttributes());
		User user = userService.upsert(profile);

		Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
		attributes.put(APP_USER_ID, user.getId());
		return new DefaultOAuth2User(List.of(), attributes, APP_USER_ID);
	}

}
