package com.fridgebot.household;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record HouseholdMemberId(
		@Column(name = "household_id") Long householdId,
		@Column(name = "user_id") Long userId) implements Serializable {
}
