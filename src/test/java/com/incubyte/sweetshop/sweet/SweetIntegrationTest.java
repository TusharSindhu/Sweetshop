package com.incubyte.sweetshop.sweet;

import com.incubyte.sweetshop.auth.AuthResponse;
import com.incubyte.sweetshop.auth.LoginRequest;
import com.incubyte.sweetshop.auth.RegisterRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        scripts = "/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class SweetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        SweetRequest sweetReq = new SweetRequest("Chocolate Lava", "Cake", new BigDecimal("5.50"), 10);

        mockMvc.perform(post("/api/sweets")
                        .header("Authorization", "Bearer " + jwtToken) // <--- Sending the token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sweetReq)))
                // 3. Assert: Expect 201 Created
                .andExpect(status().isCreated());
    }

    @Test
    void should_list_all_sweets_for_authenticated_user() throws Exception {
        // 1. Arrange: Register/Login and Add 2 Sweets
        // (Helper method to get token - reusing logic to keep test clean)
        String jwtToken = registerAndLogin();

        // Add Sweet 1 (Kaju Katli)
        SweetRequest sweet1 = new SweetRequest("Kaju Katli", "Barfi", new BigDecimal("800.00"), 50);
        mockMvc.perform(post("/api/sweets")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sweet1)))
                .andExpect(status().isCreated());

        // Add Sweet 2 (Gulab Jamun)
        SweetRequest sweet2 = new SweetRequest("Gulab Jamun", "Syrup", new BigDecimal("400.00"), 30);
        mockMvc.perform(post("/api/sweets")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sweet2)))
                .andExpect(status().isCreated());

        // 2. Act: GET /api/sweets
        mockMvc.perform(get("/api/sweets")
                        .header("Authorization", "Bearer " + jwtToken))
                // 3. Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Kaju Katli"))
                .andExpect(jsonPath("$[1].name").value("Gulab Jamun"));
    }

    // Helper method to reduce code duplication in tests
    private String registerAndLogin() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("listUser", "pass123", "list@example.com");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)));

        LoginRequest loginReq = new LoginRequest("listUser", "pass123");
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, AuthResponse.class).jwtToken();
    }
}
