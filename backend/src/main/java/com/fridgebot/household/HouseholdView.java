package com.fridgebot.household;

import java.time.Instant;
import java.util.List;

/** API 응답용. 지연 로딩된 구성원을 트랜잭션 안에서 다 읽어 둔 값. */
public record HouseholdView(Long id, String name, String inviteCode, Instant inviteCodeExpiresAt,
		List<Member> members) {

	public record Member(Long userId, String nickname, HouseholdRole role, Instant joinedAt) {
	}

}
