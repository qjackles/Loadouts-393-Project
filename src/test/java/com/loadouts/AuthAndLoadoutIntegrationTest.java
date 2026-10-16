package com.loadouts;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
class AuthAndLoadoutIntegrationTest {
  @Autowired MockMvc mvc; @Autowired ObjectMapper mapper;
  @Test void canRegisterThenCreateAndViewLoadout() throws Exception {
    MockHttpSession s=new MockHttpSession();
    mvc.perform(post("/api/auth/register").session(s).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"test@example.com\",\"username\":\"tester\",\"password\":\"password123\"}"))
      .andExpect(status().isCreated()).andExpect(jsonPath("$.username").value("tester"));
    String body=mvc.perform(post("/api/loadouts").session(s).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Beginner Baking Setup\",\"description\":\"Basic baking equipment\",\"category\":\"Cooking\"}"))
      .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    String id=mapper.readTree(body).get("id").asText();
    mvc.perform(get("/api/loadouts/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.ownerUsername").value("tester"));
  }
  @Test void creatingLoadoutWithoutLoginReturns401() throws Exception {
    mvc.perform(post("/api/loadouts").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"No Login\"}"))
      .andExpect(status().isUnauthorized());
  }
}
