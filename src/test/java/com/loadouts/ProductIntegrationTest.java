package com.loadouts;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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
class ProductIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @Test void creatorCanAddAndRemoveManualProductWhileVisitorsCanViewIt() throws Exception {
    MockHttpSession owner=new MockHttpSession();
    String unique=UUID.randomUUID().toString().replace("-", "");
    mvc.perform(post("/api/auth/register").session(owner).contentType(MediaType.APPLICATION_JSON)
      .content("{\"email\":\""+unique+"@example.com\",\"username\":\""+unique+"\",\"password\":\"password123\"}"))
      .andExpect(status().isCreated());
    String body=mvc.perform(post("/api/loadouts").session(owner).contentType(MediaType.APPLICATION_JSON)
      .content("{\"title\":\"Manual products\"}"))
      .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    String path="/api/loadouts/"+mapper.readTree(body).get("id").asText()+"/products";

    mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
      .content("{\"name\":\"Skillet\",\"url\":\"https://example.com/skillet\"}"))
      .andExpect(status().isUnauthorized());
    mvc.perform(post(path).session(owner).contentType(MediaType.APPLICATION_JSON)
      .content("{\"name\":\"Skillet\",\"url\":\"javascript:alert(1)\"}"))
      .andExpect(status().isBadRequest());

    String product=mvc.perform(post(path).session(owner).contentType(MediaType.APPLICATION_JSON)
      .content("{\"name\":\"Skillet\",\"url\":\"https://example.com/skillet\",\"note\":\"My favorite pan\"}"))
      .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Skillet"))
      .andReturn().getResponse().getContentAsString();
    String productId=mapper.readTree(product).get("id").asText();
    MockHttpSession other=new MockHttpSession();
    String otherName=UUID.randomUUID().toString().replace("-", "");
    mvc.perform(post("/api/auth/register").session(other).contentType(MediaType.APPLICATION_JSON)
      .content("{\"email\":\""+otherName+"@example.com\",\"username\":\""+otherName+"\",\"password\":\"password123\"}"))
      .andExpect(status().isCreated());
    mvc.perform(post(path).session(other).contentType(MediaType.APPLICATION_JSON)
      .content("{\"name\":\"Other\",\"url\":\"https://example.com/other\"}"))
      .andExpect(status().isForbidden());
    mvc.perform(get(path)).andExpect(status().isOk())
      .andExpect(jsonPath("$[0].url").value("https://example.com/skillet"))
      .andExpect(jsonPath("$[0].note").value("My favorite pan"));
    mvc.perform(delete(path+"/"+productId)).andExpect(status().isUnauthorized());
    mvc.perform(delete(path+"/"+productId).session(other)).andExpect(status().isForbidden());
    mvc.perform(delete(path+"/"+productId).session(owner)).andExpect(status().isNoContent());
    mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
  }
}
