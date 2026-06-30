package com.event.ticketing.booking_service.config;

import com.event.ticketing.booking_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.authorizeHttpRequests(auth -> auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
				.permitAll().requestMatchers("/actuator/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/bookings/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.GET, "/api/bookings/my").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.PUT, "/api/bookings/*/cancel").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.GET, "/api/bookings/*").hasAnyRole("CUSTOMER", "ADMIN", "SUPER_ADMIN")
				.anyRequest().authenticated());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
