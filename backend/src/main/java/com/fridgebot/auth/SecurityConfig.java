package com.fridgebot.auth;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, AppOAuth2UserService oAuth2UserService,
			OAuth2LoginHandlers loginHandlers, AuthProperties properties) throws Exception {
		ProblemJsonSecurityHandlers problemHandlers = new ProblemJsonSecurityHandlers();
		http
			// API는 Authorization 헤더의 Bearer 토큰으로만 인증하므로 CSRF 토큰이 필요 없다.
			// 리프레시 쿠키는 SameSite=Strict, Path=/api/auth 로 범위를 좁혔다.
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(corsConfigurationSource(properties)))
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/actuator/health", "/actuator/health/**", "/error").permitAll()
				.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/auth/token", "/api/auth/refresh", "/api/auth/logout")
				.permitAll()
				.requestMatchers("/api/**").authenticated()
				.anyRequest().denyAll())
			.oauth2Login(oauth -> oauth
				.userInfoEndpoint(u -> u.userService(oAuth2UserService))
				.successHandler(loginHandlers)
				.failureHandler(loginHandlers))
			.oauth2ResourceServer(rs -> rs
				.jwt(Customizer.withDefaults())
				.authenticationEntryPoint(problemHandlers)
				.accessDeniedHandler(problemHandlers))
			// oauth2Login이 등록하는 로그인 페이지 리다이렉트 대신, API에는 항상 401 JSON을 돌려준다.
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(problemHandlers)
				.accessDeniedHandler(problemHandlers));
		return http.build();
	}

	private static CorsConfigurationSource corsConfigurationSource(AuthProperties properties) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(properties.frontendUrl()));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}

}
