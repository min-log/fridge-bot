package com.fridgebot.household;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fridgebot.common.ApiException;
import com.fridgebot.user.User;
import com.fridgebot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 가구 범위는 항상 "요청한 사용자가 속한 가구"로 서버가 정한다. 요청에서 household_id를 받지 않으므로
 * 다른 가구를 지정해 접근하는 경로 자체가 없다.
 */
@Service
@RequiredArgsConstructor
public class HouseholdService {

	private final HouseholdRepository householdRepository;

	private final UserRepository userRepository;

	private final InviteCodeGenerator inviteCodeGenerator;

	private final Clock clock;

	@Transactional
	public HouseholdView create(Long userId, String name) {
		requireNoHousehold(userId);
		Household household = householdRepository.save(
				new Household(name.strip(), inviteCodeGenerator.generate(), clock.instant()));
		household.addOwner(userId, clock.instant());
		return toView(flushOrConflict(household));
	}

	@Transactional(readOnly = true)
	public HouseholdView getMine(Long userId) {
		return toView(mine(userId));
	}

	@Transactional
	public HouseholdView join(Long userId, String inviteCode) {
		requireNoHousehold(userId);
		String code = inviteCode.strip().toUpperCase(Locale.ROOT);
		Household household = householdRepository.findByInviteCode(code)
			.orElseThrow(() -> ApiException.notFound("INVALID_INVITE_CODE", "초대 코드가 올바르지 않습니다."));
		household.join(userId, code, clock.instant());
		return toView(flushOrConflict(household));
	}

	@Transactional
	public HouseholdView regenerateInviteCode(Long userId) {
		Household household = mine(userId);
		household.regenerateInviteCode(userId, inviteCodeGenerator.generate(), clock.instant());
		return toView(household);
	}

	@Transactional
	public void removeMember(Long actorId, Long targetUserId) {
		Household household = mine(actorId);
		boolean empty = household.removeMember(actorId, targetUserId);
		if (empty) {
			householdRepository.delete(household);
		}
	}

	private Household mine(Long userId) {
		return householdRepository.findByMemberUserId(userId)
			.orElseThrow(() -> ApiException.notFound("NOT_IN_HOUSEHOLD", "아직 속한 가구가 없습니다."));
	}

	private void requireNoHousehold(Long userId) {
		if (householdRepository.findByMemberUserId(userId).isPresent()) {
			throw ApiException.conflict("ALREADY_IN_HOUSEHOLD", "이미 다른 가구에 속해 있습니다.");
		}
	}

	/** 같은 사용자의 동시 요청은 DB 유니크 제약(household_members.user_id)이 최종으로 막는다. */
	private Household flushOrConflict(Household household) {
		try {
			householdRepository.flush();
			return household;
		}
		catch (DataIntegrityViolationException ex) {
			throw ApiException.conflict("ALREADY_IN_HOUSEHOLD", "이미 다른 가구에 속해 있습니다.");
		}
	}

	private HouseholdView toView(Household household) {
		List<HouseholdMember> members = household.getMembers();
		Map<Long, User> users = userRepository.findAllById(members.stream().map(HouseholdMember::getUserId).toList())
			.stream()
			.collect(Collectors.toMap(User::getId, Function.identity()));
		return new HouseholdView(household.getId(), household.getName(), household.getInviteCode(),
				household.getInviteCodeExpiresAt(),
				members.stream()
					.map(m -> new HouseholdView.Member(m.getUserId(), users.get(m.getUserId()).getNickname(),
							m.getRole(), m.getJoinedAt()))
					.toList());
	}

}
