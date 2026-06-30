package com.event.ticketing.user_service.service;

import com.event.ticketing.user_service.dto.AuthResponse;
import com.event.ticketing.user_service.dto.LoginRequestDTO;
import com.event.ticketing.user_service.dto.RegisterRequestDTO;

public interface UserAuthService {
	AuthResponse register(RegisterRequestDTO request);

	AuthResponse login(LoginRequestDTO request);
}
