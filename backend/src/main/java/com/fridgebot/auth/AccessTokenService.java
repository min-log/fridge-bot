package com.fridgebot.auth;

import java.time.Clock;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

	private final JwtEncoder jwtEncoder;

	private final AuthProperties properties;

	private final Clock clock;

	public AccessToken issue(Long userId) {
		Instant now = clock.instant();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(JwtConfig.ISSUER)
			.subject(String.valueOf(userId))
			.issuedAt(now)
			.expiresAt(now.plus(properties.accessTokenTtl()))
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new AccessToken(value, properties.accessTokenTtl().toSeconds());
	}

	public record AccessToken(String value, long expiresInSeconds) {
	}

}
