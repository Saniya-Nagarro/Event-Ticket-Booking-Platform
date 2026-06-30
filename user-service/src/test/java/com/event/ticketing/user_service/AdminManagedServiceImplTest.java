package com.event.ticketing.user_service;

import com.event.ticketing.user_service.dto.CreateUserRequestDTO;
import com.event.ticketing.user_service.dto.UpdateUserRequestDTO;
import com.event.ticketing.user_service.dto.UserResponse;
import com.event.ticketing.user_service.entity.Role;
import com.event.ticketing.user_service.entity.User;
import com.event.ticketing.user_service.repository.RoleRepository;
import com.event.ticketing.user_service.repository.UserRepository;
import com.event.ticketing.user_service.serviceimpl.AdminManagedServiceImpl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminManagedServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AdminManagedServiceImpl service;

	private Role adminRole;
	private Role customerRole;
	private Role superAdminRole;

	@BeforeEach
	void setUp() {
		adminRole = Role.builder().id(1L).name("ADMIN").build();
		customerRole = Role.builder().id(2L).name("CUSTOMER").build();
		superAdminRole = Role.builder().id(3L).name("SUPER_ADMIN").build();
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	private User user(Long id, String email, Role role) {
		return User.builder().id(id).name("Test User").email(email).password("encoded-password").role(role).active(true)
				.build();
	}

	private void mockLoggedInUser(String email) {
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList()));
	}

	@Test
	void getAllUsers_ShouldReturnUsers() {
		User user1 = user(1L, "admin@gmail.com", adminRole);
		User user2 = user(2L, "customer@gmail.com", customerRole);

		when(userRepository.findAll()).thenReturn(List.of(user1, user2));

		List<UserResponse> result = service.getAllUsers();

		assertEquals(2, result.size());
		assertEquals("admin@gmail.com", result.get(0).getEmail());
		assertEquals("CUSTOMER", result.get(1).getRole());
	}

	@Test
	void getUserById_ShouldReturnUser() {
		User user = user(1L, "admin@gmail.com", adminRole);

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		UserResponse response = service.getUserById(1L);

		assertEquals(1L, response.getId());
		assertEquals("admin@gmail.com", response.getEmail());
		assertEquals("ADMIN", response.getRole());
	}

	@Test
	void getUserById_ShouldThrowException_WhenUserNotFound() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getUserById(1L));

		assertEquals("User not found", ex.getMessage());
	}

	@Test
	void updateUser_ShouldUpdateNameAndActiveStatus() {
		User user = user(1L, "admin@gmail.com", adminRole);

		UpdateUserRequestDTO request = new UpdateUserRequestDTO();
		request.setName("Updated Name");
		request.setActive(false);

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		UserResponse response = service.updateUser(1L, request);

		assertEquals("Updated Name", response.getName());
		assertFalse(response.getActive());
		verify(userRepository).save(user);
	}

	@Test
	void deleteUser_ShouldDeleteUser() {
		User user = user(1L, "admin@gmail.com", adminRole);

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		service.deleteUser(1L);

		verify(userRepository).delete(user);
	}

	@Test
	void getLoggedInUserProfile_ShouldReturnProfile() {
		mockLoggedInUser("admin@gmail.com");

		User user = user(1L, "admin@gmail.com", adminRole);

		when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(user));

		UserResponse response = service.getLoggedInUserProfile();

		assertEquals("admin@gmail.com", response.getEmail());
		assertEquals("ADMIN", response.getRole());
	}

	@Test
	void createUser_ShouldCreateCustomer_WhenSuperAdminCreatesCustomer() {
		mockLoggedInUser("superadmin@gmail.com");

		User loggedInUser = user(1L, "superadmin@gmail.com", superAdminRole);

		CreateUserRequestDTO request = new CreateUserRequestDTO();
		request.setName("Customer One");
		request.setEmail("customer@gmail.com");
		request.setPassword("password123");
		request.setRole("CUSTOMER");

		when(userRepository.existsByEmail("customer@gmail.com")).thenReturn(false);
		when(userRepository.findByEmail("superadmin@gmail.com")).thenReturn(Optional.of(loggedInUser));
		when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
		when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UserResponse response = service.createUser(request);

		verify(userRepository).save(userCaptor.capture());

		User savedUser = userCaptor.getValue();

		assertEquals("customer@gmail.com", savedUser.getEmail());
		assertEquals("encoded-password", savedUser.getPassword());
		assertEquals("CUSTOMER", savedUser.getRole().getName());
		assertTrue(savedUser.getActive());

		assertEquals("customer@gmail.com", response.getEmail());
		assertEquals("CUSTOMER", response.getRole());
	}

	@Test
	void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
		CreateUserRequestDTO request = new CreateUserRequestDTO();
		request.setEmail("admin@gmail.com");

		when(userRepository.existsByEmail("admin@gmail.com")).thenReturn(true);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createUser(request));

		assertEquals("Email already exists", ex.getMessage());
		verify(userRepository, never()).save(any());
	}

	@Test
	void createUser_ShouldThrowException_WhenAdminCreatesAdmin() {
		mockLoggedInUser("admin@gmail.com");

		User loggedInAdmin = user(1L, "admin@gmail.com", adminRole);

		CreateUserRequestDTO request = new CreateUserRequestDTO();
		request.setName("Admin Two");
		request.setEmail("admin2@gmail.com");
		request.setPassword("admin123");
		request.setRole("ADMIN");

		when(userRepository.existsByEmail("admin2@gmail.com")).thenReturn(false);
		when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(loggedInAdmin));

		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createUser(request));

		assertEquals("ADMIN cannot create ADMIN or SUPER_ADMIN", ex.getMessage());
		verify(userRepository, never()).save(any());
	}

	@Test
	void createUser_ShouldThrowException_WhenCreatingSuperAdminFromApi() {
		mockLoggedInUser("superadmin@gmail.com");

		User loggedInUser = user(1L, "superadmin@gmail.com", superAdminRole);

		CreateUserRequestDTO request = new CreateUserRequestDTO();
		request.setName("Super Admin Two");
		request.setEmail("superadmin2@gmail.com");
		request.setPassword("password123");
		request.setRole("SUPER_ADMIN");

		when(userRepository.existsByEmail("superadmin2@gmail.com")).thenReturn(false);
		when(userRepository.findByEmail("superadmin@gmail.com")).thenReturn(Optional.of(loggedInUser));

		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createUser(request));

		assertEquals("SUPER_ADMIN cannot be created from API", ex.getMessage());
		verify(userRepository, never()).save(any());
	}

	@Test
	void createUser_ShouldThrowException_WhenRoleInvalid() {
		mockLoggedInUser("superadmin@gmail.com");

		User loggedInUser = user(1L, "superadmin@gmail.com", superAdminRole);

		CreateUserRequestDTO request = new CreateUserRequestDTO();
		request.setName("Invalid User");
		request.setEmail("invalid@gmail.com");
		request.setPassword("password123");
		request.setRole("MANAGER");

		when(userRepository.existsByEmail("invalid@gmail.com")).thenReturn(false);
		when(userRepository.findByEmail("superadmin@gmail.com")).thenReturn(Optional.of(loggedInUser));
		when(roleRepository.findByName("MANAGER")).thenReturn(Optional.empty());

		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createUser(request));

		assertEquals("Invalid role: MANAGER", ex.getMessage());
	}
}