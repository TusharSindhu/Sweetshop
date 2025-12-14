package com.incubyte.sweetshop.auth;

import com.incubyte.sweetshop.sweet.SweetRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        scripts = "/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
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

    @Test
    void should_allow_authenticated_user_to_add_sweet() throws Exception {
        // 1. Arrange: Register and Login to get a Token
        RegisterRequest registerReq = new RegisterRequest("sweetAdmin", "pass123", "admin@sweets.com");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)));

        LoginRequest loginReq = new LoginRequest("sweetAdmin", "pass123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract Token
        String jsonResponse = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(jsonResponse, AuthResponse.class);
        String jwtToken = authResponse.jwtToken();

        // 2. Act: Try to add a sweet using the token
        SweetRequest sweetReq = new SweetRequest("Kaju Katli", "Barfi", new BigDecimal("5.50"), 10);

        mockMvc.perform(post("/api/sweets")
                        .header("Authorization", "Bearer " + jwtToken) // <--- Sending the token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sweetReq)))
                // 3. Assert: Expect 201 Created
                .andExpect(status().isCreated());
    }
}
