
package com.loadouts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoadoutValidationTest {

    @Autowired
    private MockMvc mvc;

    // Helper: register a unique user for each test
    private String registerUser(MockHttpSession session) throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "");
        String email = "user" + unique + "@example.com";

        mvc.perform(post("/api/auth/register")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "username": "user%s",
                      "password": "password123"
                    }
                    """.formatted(email, unique)))
            .andExpect(status().isCreated());

        return email;
    }

    // Test 1: Password shorter than 8 characters
    @Test
    void registrationRejectsShortPassword() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "short@example.com",
                      "username": "shortpassworduser",
                      "password": "123"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("Password must be at least 8 characters."));
    }

    // Test 2: Incorrect login credentials
    @Test
    void loginRejectsWrongPassword() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String email = registerUser(session);

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "wrongpassword"
                    }
                    """.formatted(email)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("Invalid email or password."));
    }

    // Test 3: Cannot create a loadout without a title
    @Test
    void createLoadoutRejectsBlankTitle() throws Exception {
        MockHttpSession session = new MockHttpSession();
        registerUser(session);

        mvc.perform(post("/api/loadouts")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "",
                      "description": "Test description",
                      "category": "Gaming"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("title is required."));
    }

    // Test 4: Nonexistent loadout should return 404
    @Test
    void getNonexistentLoadoutReturns404() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mvc.perform(get("/api/loadouts/" + fakeId))
            .andExpect(status().isNotFound());
    }

    // Test 5: Anyone can browse loadouts
    @Test
    void getAllLoadoutsWithoutLogin() throws Exception {
        mvc.perform(get("/api/loadouts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
