package com.fridgebot.user;

import com.fridgebot.auth.CurrentUserId;
import com.fridgebot.common.ApiException;
import com.fridgebot.household.HouseholdRepository;
import com.fridgebot.household.HouseholdRole;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeController {

	private final UserRepository userRepository;

	private final HouseholdRepository householdRepository;

	@GetMapping("/api/me")
	@Transactional(readOnly = true)
	public MeResponse me(@CurrentUserId Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> ApiException.unauthorized("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
		HouseholdSummary household = householdRepository.findByMemberUserId(userId)
			.map(h -> new HouseholdSummary(h.getId(), h.getName(),
					h.findMember(userId).orElseThrow().getRole()))
			.orElse(null);
		return new MeResponse(user.getId(), user.getNickname(), user.getEmail(), user.getProvider(), household);
	}

	public record MeResponse(Long id, String nickname, String email, AuthProvider provider,
			HouseholdSummary household) {
	}

	public record HouseholdSummary(Long id, String name, HouseholdRole role) {
	}

}
