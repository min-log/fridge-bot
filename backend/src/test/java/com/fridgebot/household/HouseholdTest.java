package com.fridgebot.household;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import com.fridgebot.common.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HouseholdTest {

	static final Instant NOW = Instant.parse("2026-11-25T00:00:00Z");

	static final Long OWNER = 1L;

	static final Long MEMBER = 2L;

	Household household;

	@BeforeEach
	void setUp() {
		household = new Household("우리집", "ABCD2345", NOW);
		household.addOwner(OWNER, NOW);
	}

	@Test
	void inviteCodeExpiresAfterSevenDays() {
		assertThat(household.getInviteCodeExpiresAt()).isEqualTo(NOW.plus(Household.INVITE_CODE_TTL));
	}

	@Test
	void joinAddsMember() {
		household.join(MEMBER, "ABCD2345", NOW.plusSeconds(60));

		assertThat(household.findMember(MEMBER)).get().extracting(HouseholdMember::getRole)
			.isEqualTo(HouseholdRole.MEMBER);
	}

	@Test
	void joinFailsWhenCodeExpired() {
		Instant expired = NOW.plus(Household.INVITE_CODE_TTL);

		assertThatThrownBy(() -> household.join(MEMBER, "ABCD2345", expired))
			.isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("INVITE_CODE_EXPIRED"));
	}

	@Test
	void onlyOwnerCanRegenerateInviteCode() {
		household.join(MEMBER, "ABCD2345", NOW);

		assertThatThrownBy(() -> household.regenerateInviteCode(MEMBER, "NEWCODE2", NOW))
			.isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("OWNER_ONLY"));

		household.regenerateInviteCode(OWNER, "NEWCODE2", NOW.plusSeconds(10));
		assertThat(household.getInviteCode()).isEqualTo("NEWCODE2");
		assertThat(household.getInviteCodeExpiresAt()).isEqualTo(NOW.plusSeconds(10).plus(Household.INVITE_CODE_TTL));
	}

	@Test
	void memberCannotKickOthers() {
		household.join(MEMBER, "ABCD2345", NOW);

		assertThatThrownBy(() -> household.removeMember(MEMBER, OWNER))
			.isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("OWNER_ONLY"));
	}

	@Test
	void ownerKicksMember() {
		household.join(MEMBER, "ABCD2345", NOW);

		boolean empty = household.removeMember(OWNER, MEMBER);

		assertThat(empty).isFalse();
		assertThat(household.findMember(MEMBER)).isEmpty();
	}

	@Test
	void ownerCannotLeaveWhileOthersRemain() {
		household.join(MEMBER, "ABCD2345", NOW);

		assertThatThrownBy(() -> household.removeMember(OWNER, OWNER))
			.isInstanceOfSatisfying(ApiException.class,
					ex -> assertThat(ex.getCode()).isEqualTo("OWNER_CANNOT_LEAVE"));
	}

	@Test
	void soleOwnerLeavingEmptiesHousehold() {
		assertThat(household.removeMember(OWNER, OWNER)).isTrue();
	}

	@Test
	void removingNonMemberIsNotFound() {
		assertThatThrownBy(() -> household.removeMember(OWNER, 99L))
			.isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("MEMBER_NOT_FOUND"));
	}

}
