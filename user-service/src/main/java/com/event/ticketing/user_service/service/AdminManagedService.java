package com.event.ticketing.user_service.service;

import java.util.List;

import com.event.ticketing.user_service.dto.UpdateUserRequestDTO;
import com.event.ticketing.user_service.dto.CreateUserRequestDTO;
import com.event.ticketing.user_service.dto.UserResponse;

public interface AdminManagedService {
	UserResponse createUser(CreateUserRequestDTO request);

	List<UserResponse> getAllUsers();

	UserResponse getUserById(Long id);

	UserResponse updateUser(Long id, UpdateUserRequestDTO request);

	void deleteUser(Long id);

	UserResponse getLoggedInUserProfile();
}
