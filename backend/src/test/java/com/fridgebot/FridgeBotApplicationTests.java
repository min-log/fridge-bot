package com.fridgebot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * 1주차 뼈대 검증: 실제 Postgres(pgvector)·Redis 컨테이너로 컨텍스트가 뜨고,
 * Flyway가 실행되고, 배포 후 CD가 볼 /actuator/health 가 UP 인지 확인한다.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class FridgeBotApplicationTests {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	StringRedisTemplate redisTemplate;

	@Autowired
	MockMvcTester mvc;

	@Test
	void flywayEnablesPgvectorExtension() {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from pg_extension where extname = 'vector'", Integer.class);

		assertThat(count).isEqualTo(1);
	}

	@Test
	void redisIsReachable() {
		redisTemplate.opsForValue().set("test:ping", "pong");

		assertThat(redisTemplate.opsForValue().get("test:ping")).isEqualTo("pong");
	}

	@Test
	void healthIsUpWithDatabaseAndRedis() {
		assertThat(mvc.get().uri("/actuator/health"))
				.hasStatusOk()
				.bodyJson()
				.extractingPath("$.status").isEqualTo("UP");
	}

}
