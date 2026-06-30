package com.event.ticketing.eventservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JWTAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable());

		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.authorizeHttpRequests(
				auth -> auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

						// Public GET APIs
						.requestMatchers(HttpMethod.GET, "/api/events").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/events/search").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/events/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/events/*/availability").permitAll()

						// Booking service APIs
						.requestMatchers(HttpMethod.PUT, "/api/events/*/reduce-seats").authenticated()
						.requestMatchers(HttpMethod.PUT, "/api/events/*/increase-seats").authenticated()

						// Admin APIs
						.requestMatchers(HttpMethod.POST, "/api/events").hasAnyRole("ADMIN", "SUPER_ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/events/*").hasAnyRole("ADMIN", "SUPER_ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/events/*/publish").hasAnyRole("ADMIN", "SUPER_ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/events/*/cancel").hasAnyRole("ADMIN", "SUPER_ADMIN")

						.anyRequest().authenticated());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
