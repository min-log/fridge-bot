package com.fridgebot.user;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String email;

	@Column(nullable = false)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AuthProvider provider;

	@Column(nullable = false)
	private String providerId;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	public User(AuthProvider provider, String providerId, String nickname, String email) {
		this.provider = provider;
		this.providerId = providerId;
		this.nickname = nickname;
		this.email = email;
	}

	/** 제공자가 나중에 이메일을 주면 비어 있을 때만 채운다. 닉네임은 사용자가 바꿀 수 있으므로 덮어쓰지 않는다. */
	void fillEmailIfAbsent(String email) {
		if (this.email == null && email != null) {
			this.email = email;
		}
	}

}
