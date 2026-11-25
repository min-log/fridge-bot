package com.fridgebot.user;

import com.fridgebot.common.SecureRandomStrings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final int NICKNAME_MAX = 50;

	private final UserRepository userRepository;

	/** 소셜 로그인 결과로 사용자를 찾거나 새로 만든다. */
	@Transactional
	public User upsert(OAuthProfile profile) {
		if (profile.providerId() == null || profile.providerId().isBlank()) {
			throw new IllegalArgumentException("제공자 사용자 ID가 없습니다: " + profile.provider());
		}
		return userRepository.findByProviderAndProviderId(profile.provider(), profile.providerId())
			.map(user -> {
				user.fillEmailIfAbsent(profile.email());
				return user;
			})
			.orElseGet(() -> userRepository.save(
					new User(profile.provider(), profile.providerId(), nickname(profile), profile.email())));
	}

	private static String nickname(OAuthProfile profile) {
		String name = profile.nickname();
		if (name == null || name.isBlank()) {
			return "사용자-" + SecureRandomStrings.fromAlphabet("abcdefghijkmnpqrstuvwxyz23456789", 6);
		}
		name = name.strip();
		return name.length() > NICKNAME_MAX ? name.substring(0, NICKNAME_MAX) : name;
	}

}
