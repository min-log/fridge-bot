package com.fridgebot.common;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 만료 시각 계산을 테스트에서 고정할 수 있도록 Clock을 빈으로 둔다. */
@Configuration(proxyBeanMethods = false)
public class TimeConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

}
