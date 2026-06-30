package com.event.ticketing.user_service.serviceimpl;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.event.ticketing.user_service.dto.CreateUserRequestDTO;
import com.event.ticketing.user_service.dto.UpdateUserRequestDTO;
import com.event.ticketing.user_service.dto.UserResponse;
import com.event.ticketing.user_service.entity.Role;
import com.event.ticketing.user_service.entity.User;
import com.event.ticketing.user_service.repository.RoleRepository;
import com.event.ticketing.user_service.repository.UserRepository;
import com.event.ticketing.user_service.service.AdminManagedService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminManagedServiceImpl implements AdminManagedService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream().map(this::mapToResponse).toList();
	}

	@Override
	public UserResponse getUserById(Long id) {
		return mapToResponse(getUser(id));
	}

	@Override
	public UserResponse updateUser(Long id, UpdateUserRequestDTO request) {

		User user = getUser(id);

		if (request.getName() != null) {
			user.setName(request.getName());
		}

		if (request.getActive() != null) {
			user.setActive(request.getActive());
		}

		userRepository.save(user);

		return mapToResponse(user);
	}

	@Override
	public void deleteUser(Long id) {
		User user = getUser(id);
		userRepository.delete(user);
	}

	@Override
	public UserResponse getLoggedInUserProfile() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		return mapToResponse(user);
	}

	private User getUser(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
	}

	private UserResponse mapToResponse(User user) {
		return UserResponse.builder().id(user.getId()).name(user.getName()).email(user.getEmail())
				.role(user.getRole().getName()).active(user.getActive()).build();
	}

	@Override
	public UserResponse createUser(CreateUserRequestDTO request) {

	    if (userRepository.existsByEmail(request.getEmail())) {
	        throw new RuntimeException("Email already exists");
	    }

	    String loggedInUserEmail = SecurityContextHolder.getContext()
	            .getAuthentication()
	            .getName();

	    User loggedInUser = userRepository.findByEmail(loggedInUserEmail)
	            .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

	    String loggedInRole = loggedInUser.getRole().getName();
	    String requestedRole = request.getRole().toUpperCase();

	    if (loggedInRole.equals("ADMIN") &&
	            (requestedRole.equals("ADMIN") || requestedRole.equals("SUPER_ADMIN"))) {
	        throw new RuntimeException("ADMIN cannot create ADMIN or SUPER_ADMIN");
	    }

	    if (requestedRole.equals("SUPER_ADMIN")) {
	        throw new RuntimeException("SUPER_ADMIN cannot be created from API");
	    }

	    Role role = roleRepository.findByName(requestedRole)
	            .orElseThrow(() -> new RuntimeException("Invalid role: " + request.getRole()));

	    User user = User.builder()
	            .name(request.getName())
	            .email(request.getEmail())
	            .password(passwordEncoder.encode(request.getPassword()))
	            .role(role)
	            .active(true)
	            .build();

	    userRepository.save(user);

	    return mapToResponse(user);
	}

}
