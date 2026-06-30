package com.event.ticketing.user_service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;


import com.event.ticketing.user_service.config.JWTService;
import com.event.ticketing.user_service.dto.AuthResponse;
import com.event.ticketing.user_service.dto.LoginRequestDTO;
import com.event.ticketing.user_service.dto.RegisterRequestDTO;
import com.event.ticketing.user_service.entity.Role;
import com.event.ticketing.user_service.entity.User;
import com.event.ticketing.user_service.repository.RoleRepository;
import com.event.ticketing.user_service.repository.UserRepository;

import com.event.ticketing.user_service.serviceimpl.UserAuthServiceImpl;
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserAuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private UserAuthServiceImpl userAuthService;

    @Test
    void register_ShouldRegisterCustomerSuccessfully() {

        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("John");
        request.setEmail("john@gmail.com");
        request.setPassword("password123");

        Role customerRole = Role.builder()
                .id(1L)
                .name("CUSTOMER")
                .build();

        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(false);

        when(roleRepository.findByName("CUSTOMER"))
                .thenReturn(Optional.of(customerRole));

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(jwtService.generateToken("john@gmail.com", "CUSTOMER"))
                .thenReturn("jwt-token");

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response =
                userAuthService.register(request);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("John", savedUser.getName());
        assertEquals("john@gmail.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("CUSTOMER", savedUser.getRole().getName());
        assertTrue(savedUser.getActive());

        assertEquals("jwt-token", response.getToken());
        assertEquals("john@gmail.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {

        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("john@gmail.com");

        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userAuthService.register(request)
        );

        assertEquals("Email already exists", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenCustomerRoleMissing() {

        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("john@gmail.com");

        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(false);

        when(roleRepository.findByName("CUSTOMER"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userAuthService.register(request)
        );

        assertEquals("CUSTOMER role not found", ex.getMessage());
    }

    @Test
    void login_ShouldLoginSuccessfully() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("customer@gmail.com");
        request.setPassword("customer123");

        Role customerRole = Role.builder()
                .id(1L)
                .name("CUSTOMER")
                .build();

        User user = User.builder()
                .id(1L)
                .name("Customer")
                .email("customer@gmail.com")
                .password("encoded-password")
                .role(customerRole)
                .active(true)
                .build();

        when(userRepository.findByEmail("customer@gmail.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(
                "customer@gmail.com",
                "CUSTOMER"))
                .thenReturn("jwt-token");

        AuthResponse response =
                userAuthService.login(request);

        verify(authenticationManager)
                .authenticate(any(
                        UsernamePasswordAuthenticationToken.class));

        assertEquals("jwt-token", response.getToken());
        assertEquals("customer@gmail.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("unknown@gmail.com");
        request.setPassword("password");

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userAuthService.login(request)
        );

        assertEquals("User not found", ex.getMessage());

        verify(authenticationManager, never())
                .authenticate(any());
    }

    @Test
    void login_ShouldCallAuthenticationManager() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("customer@gmail.com");
        request.setPassword("customer123");

        Role role = Role.builder()
                .id(1L)
                .name("CUSTOMER")
                .build();

        User user = User.builder()
                .id(1L)
                .email("customer@gmail.com")
                .password("encoded-password")
                .role(role)
                .active(true)
                .build();

        when(userRepository.findByEmail("customer@gmail.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(anyString(), anyString()))
                .thenReturn("jwt-token");

        userAuthService.login(request);

        verify(authenticationManager, times(1))
                .authenticate(any(
                        UsernamePasswordAuthenticationToken.class));
    }
}
