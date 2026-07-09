package com.event.ticketing.user_service.integration;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.event.ticketing.user_service.entity.Role;
import com.event.ticketing.user_service.repository.RoleRepository;
import com.event.ticketing.user_service.repository.UserRepository;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAuthIntegrationTest {

    

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(Role.builder()
                .name("CUSTOMER")
                .build());
    }

    @Test
    void shouldRegisterAndLoginUser() throws Exception {

        String registerRequest = """
                {
                  "name": "Test User",
                  "email": "testuser@gmail.com",
                  "password": "test123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("testuser@gmail.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.token", notNullValue()));

        String loginRequest = """
                {
                  "email": "testuser@gmail.com",
                  "password": "test123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@gmail.com"))
                .andExpect(jsonPath("$.token", notNullValue()));
    }
}
