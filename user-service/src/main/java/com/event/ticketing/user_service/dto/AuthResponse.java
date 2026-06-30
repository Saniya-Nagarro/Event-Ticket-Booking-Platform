package com.event.ticketing.user_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

	private String token;
	private Long userId;
	private String name;
	private String email;
	private String role;

}
