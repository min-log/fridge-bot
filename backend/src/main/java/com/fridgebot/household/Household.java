package com.fridgebot.household;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fridgebot.common.ApiException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 가구(냉장고를 함께 쓰는 단위). 구성원 추가·내보내기 규칙을 이 안에 모아
 * 서비스 없이 단위 테스트할 수 있게 한다.
 */
@Entity
@Table(name = "households")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Household {

	public static final Duration INVITE_CODE_TTL = Duration.ofDays(7);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String inviteCode;

	@Column(nullable = false)
	private Instant inviteCodeExpiresAt;

	@OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("joinedAt")
	private List<HouseholdMember> members = new ArrayList<>();

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	public Household(String name, String inviteCode, Instant now) {
		this.name = name;
		this.inviteCode = inviteCode;
		this.inviteCodeExpiresAt = now.plus(INVITE_CODE_TTL);
	}

	/** 만든 사람을 OWNER로 넣는다. id가 정해진 뒤(저장 후)에 호출한다. */
	void addOwner(Long userId, Instant now) {
		members.add(new HouseholdMember(this, userId, HouseholdRole.OWNER, now));
	}

	void join(Long userId, String code, Instant now) {
		if (!inviteCode.equals(code)) {
			throw ApiException.notFound("INVALID_INVITE_CODE", "초대 코드가 올바르지 않습니다.");
		}
		if (!now.isBefore(inviteCodeExpiresAt)) {
			throw ApiException.badRequest("INVITE_CODE_EXPIRED", "초대 코드가 만료되었습니다. 새 코드를 받아 주세요.");
		}
		if (findMember(userId).isPresent()) {
			throw ApiException.conflict("ALREADY_IN_HOUSEHOLD", "이미 이 가구의 구성원입니다.");
		}
		members.add(new HouseholdMember(this, userId, HouseholdRole.MEMBER, now));
	}

	void regenerateInviteCode(Long actorId, String newCode, Instant now) {
		requireOwner(actorId);
		this.inviteCode = newCode;
		this.inviteCodeExpiresAt = now.plus(INVITE_CODE_TTL);
	}

	/**
	 * 구성원을 내보내거나 본인이 나간다.
	 * @return 가구에 아무도 남지 않으면 true (호출한 쪽이 가구를 삭제)
	 */
	boolean removeMember(Long actorId, Long targetId) {
		HouseholdMember target = findMember(targetId)
			.orElseThrow(() -> ApiException.notFound("MEMBER_NOT_FOUND", "이 가구의 구성원이 아닙니다."));
		if (!actorId.equals(targetId)) {
			requireOwner(actorId);
		}
		else if (target.isOwner() && members.size() > 1) {
			// MVP에는 소유자 넘기기가 없으므로 다른 구성원이 있으면 소유자는 나갈 수 없다.
			throw ApiException.conflict("OWNER_CANNOT_LEAVE", "다른 구성원을 먼저 내보낸 뒤 나갈 수 있습니다.");
		}
		members.remove(target);
		return members.isEmpty();
	}

	public Optional<HouseholdMember> findMember(Long userId) {
		return members.stream().filter(m -> m.getUserId().equals(userId)).findFirst();
	}

	private void requireOwner(Long actorId) {
		boolean owner = findMember(actorId).map(HouseholdMember::isOwner).orElse(false);
		if (!owner) {
			throw ApiException.forbidden("OWNER_ONLY", "가구 소유자만 할 수 있습니다.");
		}
	}

}
