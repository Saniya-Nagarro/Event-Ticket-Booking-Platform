package com.event.ticketing.user_service.serviceimpl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.event.ticketing.user_service.config.JWTService;
import com.event.ticketing.user_service.dto.AuthResponse;
import com.event.ticketing.user_service.dto.LoginRequestDTO;
import com.event.ticketing.user_service.dto.RegisterRequestDTO;
import com.event.ticketing.user_service.entity.Role;
import com.event.ticketing.user_service.entity.User;
import com.event.ticketing.user_service.repository.RoleRepository;
import com.event.ticketing.user_service.repository.UserRepository;
import com.event.ticketing.user_service.service.UserAuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JWTService jwtService;

	@Override
	public AuthResponse register(RegisterRequestDTO request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Email already exists");
		}

		Role customerRole = roleRepository.findByName("CUSTOMER")
				.orElseThrow(() -> new RuntimeException("CUSTOMER role not found"));

		User user = User.builder().name(request.getName()).email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword())).role(customerRole).active(true).build();

		userRepository.save(user);

		String token = jwtService.generateToken(user.getEmail(), user.getRole().getName());

		return AuthResponse.builder().token(token).userId(user.getId()).name(user.getName()).email(user.getEmail())
				.role(user.getRole().getName()).build();
	}

//	@Override
//	public AuthResponse login(LoginRequestDTO request) {
//		System.out.println(new BCryptPasswordEncoder().encode("superadmin123"));
//		authenticationManager
//				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
//
//		User user = userRepository.findByEmail(request.getEmail())
//				.orElseThrow(() -> new RuntimeException("User not found"));
//
//		String token = jwtService.generateToken(user.getEmail(), user.getRole().getName());
//
//		return AuthResponse.builder().token(token).userId(user.getId()).name(user.getName()).email(user.getEmail())
//				.role(user.getRole().getName()).build();
//	}

	@Override
	public AuthResponse login(LoginRequestDTO request) {

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));

		System.out.println("Email from request: " + request.getEmail());
		System.out.println("Password from request: " + request.getPassword());
		System.out.println("Password from DB: " + user.getPassword());

		System.out.println(
				"Password matches: " + new BCryptPasswordEncoder().matches(request.getPassword(), user.getPassword()));

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		String token = jwtService.generateToken(user.getEmail(), user.getRole().getName());

		return AuthResponse.builder().token(token).userId(user.getId()).name(user.getName()).email(user.getEmail())
				.role(user.getRole().getName()).build();
	}
}
