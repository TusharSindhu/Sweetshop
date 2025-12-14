package com.incubyte.sweetshop.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_register_new_user_successfully() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("testuser", "password123", "test@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated()); // Expecting HTTP 201 Created
    }

    @Test
    void should_login_existing_user_and_return_jwt_token() throws Exception {
        // 1. Arrange: Register a user first to ensure a valid credential set exists in the DB
        RegisterRequest registerRequest = new RegisterRequest("loginUser", "securePass", "login@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Act: Attempt to log in with the new credentials
        LoginRequest loginRequest = new LoginRequest("loginUser", "securePass");

        // 3. Assert: Check for 200 OK and the presence of a JWT token
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                // Expecting a 200 OK response
                .andExpect(status().isOk())
                // Crucial assertion: Fails now because the endpoint doesn't exist and no token is returned
                .andExpect(jsonPath("$.jwtToken").exists())
                .andExpect(jsonPath("$.username").value("loginUser"));
    }
}
