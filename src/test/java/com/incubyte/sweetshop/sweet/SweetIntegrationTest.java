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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private com.incubyte.sweetshop.user.UserRepository userRepository; // Add this

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

    @Test
    void should_decrease_quantity_when_sweet_is_purchased() throws Exception {
        // 1. Arrange: Login and Add a Sweet with quantity 10
        String jwtToken = registerAndLogin();

        SweetRequest sweetReq = new SweetRequest("Rasmalai", "Bengali", new BigDecimal("50.00"), 10);

        // We need the ID of the created sweet to buy it.
        // We capture the response to extract the ID.
        String sweetResponseJson = mockMvc.perform(post("/api/sweets")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sweetReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        SweetResponse createdSweet = objectMapper.readValue(sweetResponseJson, SweetResponse.class);
        Long sweetId = createdSweet.id();

        // 2. Act: Purchase the sweet (POST /api/sweets/{id}/purchase)
        mockMvc.perform(post("/api/sweets/" + sweetId + "/purchase")
                        .header("Authorization", "Bearer " + jwtToken))
                // 3. Assert: Expect 200 OK
                .andExpect(status().isOk());

        // 4. Verify: Fetch the sweet again and check if quantity is now 9
        mockMvc.perform(get("/api/sweets")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(jsonPath("$[0].name").value("Rasmalai"))
                .andExpect(jsonPath("$[0].quantity").value(9)); // <--- Crucial Check
    }

    @Test
    void should_search_sweets_by_name() throws Exception {
        // 1. Arrange: Add two different sweets
        String jwtToken = registerAndLogin();

        // Add Kaju Katli
        mockMvc.perform(post("/api/sweets")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new SweetRequest("Kaju Katli", "Barfi", new BigDecimal("800.00"), 50))));

        // Add Gulab Jamun
        mockMvc.perform(post("/api/sweets")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new SweetRequest("Gulab Jamun", "Syrup", new BigDecimal("400.00"), 30))));

        // 2. Act: Search for "Kaju"
        mockMvc.perform(get("/api/sweets/search")
                        .param("name", "Kaju") // Query Parameter
                        .header("Authorization", "Bearer " + jwtToken))
                // 3. Assert: Should find 1, but NOT 2
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Kaju Katli"));
    }

    @Test
    void should_allow_only_admin_to_delete_sweet() throws Exception {
        // 1. Arrange: Create a Sweet as a normal user
        String userToken = registerAndLogin("normalUser", "pass", "user@test.com");

        // Add a sweet (assuming normal users can add for now, or use a helper to seed DB)
        // Note: For this test, we'll just seed the sweet directly or use the user token if allowed
        SweetRequest sweetReq = new SweetRequest("Old Sweet", "Stale", new BigDecimal("10.00"), 5);
        String response = mockMvc.perform(post("/api/sweets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sweetReq)))
                .andReturn().getResponse().getContentAsString();

        Long sweetId = objectMapper.readValue(response, SweetResponse.class).id();

        // 2. Act & Assert: Normal User tries to DELETE -> 403 Forbidden
        mockMvc.perform(delete("/api/sweets/" + sweetId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // 3. Arrange: Create an Admin User
        // Register first
        RegisterRequest adminReq = new RegisterRequest("adminUser", "adminPass", "admin@test.com");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminReq)));

        // MANUALLY Promote to Admin in DB
        com.incubyte.sweetshop.user.User adminUser = userRepository.findByUsername("adminUser").get();
        adminUser.setRole("ROLE_ADMIN");
        userRepository.save(adminUser);

        // Login as Admin
        LoginRequest loginReq = new LoginRequest("adminUser", "adminPass");
        String adminToken = objectMapper.readValue(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq)))
                        .andReturn().getResponse().getContentAsString(),
                AuthResponse.class).jwtToken();

        // 4. Act & Assert: Admin tries to DELETE -> 204 No Content
        mockMvc.perform(delete("/api/sweets/" + sweetId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    // Helper update
    private String registerAndLogin(String username, String pass, String email) throws Exception {
        RegisterRequest registerReq = new RegisterRequest(username, pass, email);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)));

        LoginRequest loginReq = new LoginRequest(username, pass);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, AuthResponse.class).jwtToken();
    }

    @Test
    void should_allow_only_admin_to_restock_sweet() throws Exception {
        // 1. Arrange: Create Sweet with low stock (5)
        // We need a helper to register/login as Admin quickly
        String adminToken = registerAndLogin("restockAdmin", "pass", "admin@stock.com");

        // Promote to Admin
        com.incubyte.sweetshop.user.User adminUser = userRepository.findByUsername("restockAdmin").get();
        adminUser.setRole("ROLE_ADMIN");
        userRepository.save(adminUser);

        // Re-login to get updated token with ROLE_ADMIN
        LoginRequest loginReq = new LoginRequest("restockAdmin", "pass");
        adminToken = objectMapper.readValue(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq)))
                        .andReturn().getResponse().getContentAsString(),
                AuthResponse.class).jwtToken();

        // Create the sweet
        SweetRequest sweetReq = new SweetRequest("Empty Ladoo", "Ladoo", new BigDecimal("10.00"), 5);
        String response = mockMvc.perform(post("/api/sweets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sweetReq)))
                .andReturn().getResponse().getContentAsString();
        Long sweetId = objectMapper.readValue(response, SweetResponse.class).id();

        // 2. Act: Admin adds 20 items
        // We'll send a simple Map or JSON for the quantity to add
        String restockBody = "{\"quantity\": 20}";

        mockMvc.perform(post("/api/sweets/" + sweetId + "/restock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restockBody))
                .andExpect(status().isOk());

        // 3. Assert: Check new quantity (5 + 20 = 25)
        mockMvc.perform(get("/api/sweets")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$[0].name").value("Empty Ladoo"))
                .andExpect(jsonPath("$[0].quantity").value(25));
    }
}
