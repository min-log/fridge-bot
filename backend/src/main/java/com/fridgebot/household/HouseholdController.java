package com.fridgebot.household;

import com.fridgebot.auth.CurrentUserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/households")
@RequiredArgsConstructor
public class HouseholdController {

	private final HouseholdService householdService;

	@PostMapping
	public ResponseEntity<HouseholdView> create(@CurrentUserId Long userId,
			@Valid @RequestBody CreateHouseholdRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(householdService.create(userId, request.name()));
	}

	@GetMapping("/me")
	public HouseholdView mine(@CurrentUserId Long userId) {
		return householdService.getMine(userId);
	}

	@PostMapping("/invite-code")
	public HouseholdView regenerateInviteCode(@CurrentUserId Long userId) {
		return householdService.regenerateInviteCode(userId);
	}

	@PostMapping("/join")
	public HouseholdView join(@CurrentUserId Long userId, @Valid @RequestBody JoinHouseholdRequest request) {
		return householdService.join(userId, request.inviteCode());
	}

	/** 소유자가 구성원을 내보내거나, 본인 ID로 호출하면 가구에서 나간다. */
	@DeleteMapping("/members/{targetUserId}")
	public ResponseEntity<Void> removeMember(@CurrentUserId Long userId, @PathVariable Long targetUserId) {
		householdService.removeMember(userId, targetUserId);
		return ResponseEntity.noContent().build();
	}

	public record CreateHouseholdRequest(@NotBlank @Size(max = 50) String name) {
	}

	public record JoinHouseholdRequest(@NotBlank @Size(max = 16) String inviteCode) {
	}

}
