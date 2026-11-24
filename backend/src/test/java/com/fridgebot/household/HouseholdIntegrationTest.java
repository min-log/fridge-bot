package com.fridgebot.household;

import static org.assertj.core.api.Assertions.assertThat;

import com.fridgebot.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class HouseholdIntegrationTest extends IntegrationTestSupport {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void createInviteAndJoin() {
		Long owner = newUser("엄마");
		Long member = newUser("아이");

		MvcTestResult created = post(owner, "/api/households", "{\"name\":\"우리집\"}");
		assertThat(created).hasStatus(HttpStatus.CREATED);
		String inviteCode = JsonPath.read(body(created), "$.inviteCode");

		// 소문자·공백이 섞여도 참여할 수 있다
		assertThat(post(member, "/api/households/join", "{\"inviteCode\":\" " + inviteCode.toLowerCase() + " \"}"))
			.hasStatusOk()
			.bodyJson()
			.extractingPath("$.members[*].nickname")
			.asArray()
			.containsExactly("엄마", "아이");

		assertThat(get(member, "/api/me")).hasStatusOk()
			.bodyJson()
			.extractingPath("$.household.role")
			.isEqualTo("MEMBER");
	}

	@Test
	void userCannotBelongToTwoHouseholds() {
		Long owner = newUser("엄마");
		createHousehold(owner);

		assertThat(post(owner, "/api/households", "{\"name\":\"두번째\"}")).hasStatus(HttpStatus.CONFLICT)
			.bodyJson().extractingPath("$.code").isEqualTo("ALREADY_IN_HOUSEHOLD");

		String otherCode = createHousehold(newUser("이웃"));
		assertThat(post(owner, "/api/households/join", "{\"inviteCode\":\"" + otherCode + "\"}"))
			.hasStatus(HttpStatus.CONFLICT);
	}

	@Test
	void expiredOrUnknownInviteCodeIsRejected() {
		String code = createHousehold(newUser("엄마"));
		jdbcTemplate.update("update households set invite_code_expires_at = now() - interval '1 minute' "
				+ "where invite_code = ?", code);

		assertThat(post(newUser("아이"), "/api/households/join", "{\"inviteCode\":\"" + code + "\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson().extractingPath("$.code").isEqualTo("INVITE_CODE_EXPIRED");

		assertThat(post(newUser("아이"), "/api/households/join", "{\"inviteCode\":\"NOPE2345\"}"))
			.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void onlyOwnerRegeneratesInviteCodeAndOldCodeStopsWorking() {
		Long owner = newUser("엄마");
		Long member = newUser("아이");
		String oldCode = createHousehold(owner);
		join(member, oldCode);

		assertThat(post(member, "/api/households/invite-code", "")).hasStatus(HttpStatus.FORBIDDEN)
			.bodyJson().extractingPath("$.code").isEqualTo("OWNER_ONLY");

		String newCode = JsonPath.read(body(post(owner, "/api/households/invite-code", "")), "$.inviteCode");
		assertThat(newCode).isNotEqualTo(oldCode);
		assertThat(post(newUser("손님"), "/api/households/join", "{\"inviteCode\":\"" + oldCode + "\"}"))
			.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void cannotTouchMembersOfAnotherHousehold() {
		Long ownerA = newUser("A집");
		createHousehold(ownerA);
		Long ownerB = newUser("B집");
		Long memberB = newUser("B아이");
		join(memberB, createHousehold(ownerB));

		// A집 소유자가 B집 구성원을 내보내려 해도 자기 가구 안에서만 찾으므로 404
		assertThat(delete(ownerA, "/api/households/members/" + memberB)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson().extractingPath("$.code").isEqualTo("MEMBER_NOT_FOUND");
		assertThat(get(ownerB, "/api/households/me")).bodyJson()
			.extractingPath("$.members.length()").isEqualTo(2);
	}

	@Test
	void memberCannotKickButCanLeave() {
		Long owner = newUser("엄마");
		Long member = newUser("아이");
		join(member, createHousehold(owner));

		assertThat(delete(member, "/api/households/members/" + owner)).hasStatus(HttpStatus.FORBIDDEN);
		assertThat(delete(owner, "/api/households/members/" + owner)).hasStatus(HttpStatus.CONFLICT)
			.bodyJson().extractingPath("$.code").isEqualTo("OWNER_CANNOT_LEAVE");

		assertThat(delete(member, "/api/households/members/" + member)).hasStatus(HttpStatus.NO_CONTENT);
		assertThat(get(member, "/api/households/me")).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void soleOwnerLeavingDeletesHousehold() {
		Long owner = newUser("혼자");
		String code = createHousehold(owner);

		assertThat(delete(owner, "/api/households/members/" + owner)).hasStatus(HttpStatus.NO_CONTENT);
		assertThat(jdbcTemplate.queryForObject("select count(*) from households where invite_code = ?",
				Integer.class, code)).isZero();
	}

	@Test
	void validatesRequestAndRequiresLogin() {
		assertThat(post(newUser("엄마"), "/api/households", "{\"name\":\" \"}")).hasStatus(HttpStatus.BAD_REQUEST)
			.hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);

		assertThat(mvc.post().uri("/api/households").contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"우리집\"}")).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	private String createHousehold(Long owner) {
		return JsonPath.read(body(post(owner, "/api/households", "{\"name\":\"우리집\"}")), "$.inviteCode");
	}

	private void join(Long userId, String code) {
		assertThat(post(userId, "/api/households/join", "{\"inviteCode\":\"" + code + "\"}")).hasStatusOk();
	}

	private MvcTestResult post(Long userId, String uri, String json) {
		return mvc.post().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(userId))
			.contentType(MediaType.APPLICATION_JSON).content(json).exchange();
	}

	private MvcTestResult get(Long userId, String uri) {
		return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(userId)).exchange();
	}

	private MvcTestResult delete(Long userId, String uri) {
		return mvc.delete().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(userId)).exchange();
	}

	private static String body(MvcTestResult result) {
		try {
			return result.getResponse().getContentAsString();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

}
