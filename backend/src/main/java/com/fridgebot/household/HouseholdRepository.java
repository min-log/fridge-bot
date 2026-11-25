package com.fridgebot.household;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HouseholdRepository extends JpaRepository<Household, Long> {

	Optional<Household> findByInviteCode(String inviteCode);

	@Query("select m.household from HouseholdMember m where m.id.userId = :userId")
	Optional<Household> findByMemberUserId(Long userId);

}
