package com.fridgebot.household;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "household_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseholdMember {

	@EmbeddedId
	private HouseholdMemberId id;

	@MapsId("householdId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Household household;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private HouseholdRole role;

	@Column(nullable = false, updatable = false)
	private Instant joinedAt;

	HouseholdMember(Household household, Long userId, HouseholdRole role, Instant joinedAt) {
		this.id = new HouseholdMemberId(household.getId(), userId);
		this.household = household;
		this.role = role;
		this.joinedAt = joinedAt;
	}

	public Long getUserId() {
		return id.userId();
	}

	public boolean isOwner() {
		return role == HouseholdRole.OWNER;
	}

}
