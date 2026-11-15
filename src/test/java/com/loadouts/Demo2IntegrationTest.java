package com.loadouts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadouts.user.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class Demo2IntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder encoder;
  private static final String PASSWORD="DemoPass123!";
  record Account(String id,String email,String username,MockHttpSession session){}

  @Test void registrationLoginAndLogoutHaveServerOwnedSessionsAndHashedPasswords() throws Exception {
    var account=register();
    find("/api/auth/me",account.session()).andExpect(status().isOk())
      .andExpect(jsonPath("$.username").value(account.username()))
      .andExpect(jsonPath("$.passwordHash").doesNotExist());
    var stored=users.findByEmailIgnoreCase(account.email()).orElseThrow().getPasswordHash();
    assertThat(stored).isNotEqualTo(PASSWORD);
    assertThat(encoder.matches(PASSWORD,stored)).isTrue();
    send(post("/api/auth/register"),null,Map.of("email",account.email().toUpperCase(java.util.Locale.ROOT),
        "username","duplicate-"+UUID.randomUUID(),"password",PASSWORD))
        .andExpect(status().isBadRequest());
    send(post("/api/auth/register"),null,Map.of("email","fresh@example.com","username","unused-"+UUID.randomUUID(),
        "password","é".repeat(37))).andExpect(status().isBadRequest());
    var login=new MockHttpSession();
    String before=login.getId();
    send(post("/api/auth/login"),login,Map.of("email",account.email(),"password",PASSWORD)).andExpect(status().isOk());
    assertThat(login.getId()).isNotEqualTo(before);
    find("/api/auth/me",login).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(account.id()));
    mvc.perform(post("/api/auth/logout").session(login)).andExpect(status().isNoContent());
    assertThat(login.isInvalid()).isTrue();
    find("/api/auth/me",null).andExpect(status().isUnauthorized());
  }

  @Test void draftsStayPrivateUntilPublishedAndOnlyOwnerCanModifyOrDelete() throws Exception {
    var owner=register();var other=register();String title="Draft-"+UUID.randomUUID();
    String id=loadout(owner,title,"Cooking",true,List.of(Map.of("name","Bowl")));
    String path="/api/loadouts/"+id;
    find("/api/loadouts?q="+title,null).andExpect(jsonPath("$",hasSize(0)));
    find("/api/loadouts/mine",owner.session()).andExpect(jsonPath("$[0].draft").value(true));
    find(path,null).andExpect(status().isNotFound());
    find(path+"/products",other.session()).andExpect(status().isNotFound());
    find(path,owner.session()).andExpect(status().isOk());
    var update=Map.of("title",title+" revised","category","Cooking","draft",false,"description","Updated");
    send(put(path),other.session(),update).andExpect(status().isNotFound());
    send(put(path),owner.session(),update).andExpect(status().isOk()).andExpect(jsonPath("$.draft").value(false));
    find("/api/loadouts?q="+title,null).andExpect(jsonPath("$[0].id").value(id));
    send(put(path),other.session(),Map.of("title","Unauthorized")).andExpect(status().isForbidden());
    mvc.perform(delete(path).session(other.session())).andExpect(status().isForbidden());
    mvc.perform(put(path+"/like").session(other.session())).andExpect(status().isNoContent());
    mvc.perform(delete(path).session(owner.session())).andExpect(status().isNoContent());
    find(path+"/products",null).andExpect(status().isNotFound());
    find(path+"/community",null).andExpect(status().isNotFound());
  }

  @Test void productPricesDistinguishUnknownAndZeroAndTotalsFollowEdits() throws Exception {
    var owner=register();
    String id=loadout(owner,"Priced-"+UUID.randomUUID(),"Cooking",false,List.of(
        Map.of("name","Mixer","price",24.50,"store","Kitchen Store"),
        Map.of("name","Spoon"),Map.of("name","Sample","price",0)));
    String path="/api/loadouts/"+id, items=path+"/products";
    find(path,null).andExpect(jsonPath("$.knownTotal").value(24.5))
        .andExpect(jsonPath("$.pricedCount").value(2)).andExpect(jsonPath("$.missingPriceCount").value(1));
    var list=json(find(items,null).andExpect(jsonPath("$",hasSize(3))));
    String spoon=null,mixer=null;
    for(var row:list){
      if(row.path("name").asText().equals("Spoon")){
        spoon=row.path("id").asText();
        assertThat(row.path("price").isNull()).isTrue();
        assertThat(row.path("url").isNull()).isTrue();
      }
      if(row.path("name").asText().equals("Mixer"))mixer=row.path("id").asText();
    }
    assertThat(spoon).isNotNull();assertThat(mixer).isNotNull();
    send(put(items+"/"+spoon),owner.session(),Map.of("name","Spoon","price",10.25,"availability","In stock"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.availability").value("In stock"));
    find(path,null).andExpect(jsonPath("$.knownTotal").value(34.75));
    send(put(items+"/"+mixer),owner.session(),Map.of("name","Mixer","price",24.501)).andExpect(status().isBadRequest());
    find(path,null).andExpect(jsonPath("$.knownTotal").value(34.75));
    mvc.perform(delete(items+"/"+mixer).session(owner.session())).andExpect(status().isNoContent());
    find(path,null).andExpect(jsonPath("$.knownTotal").value(10.25));
  }

  @Test void invalidProductRollsBackTheWholeNewLoadout() throws Exception {
    var owner=register();
    String title="Rollback-"+UUID.randomUUID();
    send(post("/api/loadouts"),owner.session(),Map.of("title",title,"products",
        List.of(Map.of("name","Valid"),Map.of("name","Invalid","url","javascript:alert(1)"))))
        .andExpect(status().isBadRequest());
    find("/api/loadouts/mine",owner.session()).andExpect(jsonPath("$",hasSize(0)));
  }

  @Test void searchCombinesCategoryAndPriceFiltersAndDoesNotIncludeUnknownPricesInRanges() throws Exception {
    var owner=register();var prefix="Browse-"+UUID.randomUUID();
    var hi=loadout(owner,prefix+" Zulu","Outdoors",false,List.of(Map.of("name","Tent","price",80)));
    var lo=loadout(owner,prefix+" Alpha","Outdoors",false,List.of(Map.of("name","Rope","price",15)));
    var unknown=loadout(owner,prefix+" Beta","Outdoors",false,List.of(Map.of("name","Chair")));
    loadout(owner,prefix+" Cooking","Cooking",false,List.of(Map.of("name","Fork","price",40)));
    mvc.perform(get("/api/loadouts").param("q",prefix).param("category","Outdoors").param("sort","price-high"))
        .andExpect(jsonPath("$[*].id",contains(hi,lo,unknown)));
    mvc.perform(get("/api/loadouts").param("q",prefix).param("category","Outdoors").param("minPrice","15").param("maxPrice","15"))
        .andExpect(jsonPath("$[*].id",contains(lo)));
    mvc.perform(get("/api/loadouts").param("q",prefix).param("category","Outdoors").param("sort","title"))
        .andExpect(jsonPath("$[*].id",contains(lo,unknown,hi)));
    mvc.perform(get("/api/loadouts").param("minPrice","5").param("maxPrice","1")).andExpect(status().isBadRequest());
  }

  @Test void privateProfilesHidePublishedLoadoutsFromPublicDiscoveryAndDirectReads() throws Exception {
    var owner=register();var other=register();var title="Private-"+UUID.randomUUID();
    var id=loadout(owner,title,"Cooking",false,List.of(Map.of("name","Mixer")));
    String path="/api/loadouts/"+id, profile="/api/profiles/"+owner.id();
    find(profile,null).andExpect(jsonPath("$.username").value(owner.username()))
        .andExpect(jsonPath("$.loadouts[0].id").value(id))
        .andExpect(jsonPath("$.email").doesNotExist());
    send(put(profile),other.session(),Map.of("bio","Spoofed","privateProfile",true)).andExpect(status().isMethodNotAllowed());
    mvc.perform(put(path+"/bookmark").session(other.session())).andExpect(status().isNoContent());
    send(put("/api/profiles/me"),owner.session(),Map.of("displayName","QA Baker","bio","Weekend baking","privateProfile",true))
        .andExpect(status().isOk()).andExpect(jsonPath("$.privateProfile").value(true));
    find(profile,other.session()).andExpect(status().isNotFound());
    find(path,other.session()).andExpect(status().isNotFound());
    find(path+"/products",null).andExpect(status().isNotFound());
    find(path+"/community",null).andExpect(status().isNotFound());
    find(path+"/suggestions",null).andExpect(status().isNotFound());
    find("/api/loadouts?q="+title,null).andExpect(jsonPath("$",hasSize(0)));
    find("/api/loadouts/bookmarks",other.session()).andExpect(jsonPath("$",hasSize(0)));
    find(profile,owner.session()).andExpect(jsonPath("$.bio").value("Weekend baking")).andExpect(jsonPath("$.displayName").value("QA Baker"));
    find(path,owner.session()).andExpect(status().isOk());
  }

  @Test void communityControlsAreAuthenticatedAndPersistSeparatelyPerViewer() throws Exception {
    var owner=register();var visitor=register();
    String id=loadout(owner,"Community-"+UUID.randomUUID(),null,false,List.of());
    String p="/api/loadouts/"+id;
    mvc.perform(put(p+"/like")).andExpect(status().isUnauthorized());
    mvc.perform(put(p+"/bookmark")).andExpect(status().isUnauthorized());
    send(post(p+"/comments"),null,Map.of("body","Anonymous")).andExpect(status().isUnauthorized());
    send(post(p+"/reviews"),null,Map.of("body","Anonymous")).andExpect(status().isUnauthorized());
    mvc.perform(put(p+"/like").session(visitor.session()).header("Origin","https://cross.example"))
        .andExpect(status().isForbidden());
    mvc.perform(put(p+"/like").session(visitor.session())).andExpect(status().isNoContent());
    mvc.perform(put(p+"/like").session(visitor.session())).andExpect(status().isNoContent());
    mvc.perform(put(p+"/bookmark").session(visitor.session())).andExpect(status().isNoContent());
    find("/api/loadouts/bookmarks",visitor.session()).andExpect(jsonPath("$[*].id",contains(id)));
    find("/api/loadouts/bookmarks",owner.session()).andExpect(jsonPath("$",hasSize(0)));
    send(post(p+"/comments"),visitor.session(),Map.of("body","Great for beginners")).andExpect(status().isCreated());
    send(post(p+"/reviews"),visitor.session(),Map.of("body","Good starter gear")).andExpect(status().isCreated());
    send(post(p+"/reviews"),visitor.session(),Map.of("body","   ")).andExpect(status().isBadRequest());
    find(p+"/community",visitor.session()).andExpect(jsonPath("$.likes").value(1))
        .andExpect(jsonPath("$.liked").value(true)).andExpect(jsonPath("$.bookmarked").value(true));
    find(p+"/community",null).andExpect(jsonPath("$.liked").value(false)).andExpect(jsonPath("$.bookmarked").value(false))
        .andExpect(jsonPath("$.comments[0].body").value("Great for beginners"))
        .andExpect(jsonPath("$.reviews[0].body").value("Good starter gear"));
    mvc.perform(delete(p+"/like").session(visitor.session())).andExpect(status().isNoContent());
    mvc.perform(delete(p+"/bookmark").session(visitor.session())).andExpect(status().isNoContent());
    find(p+"/community",null).andExpect(jsonPath("$.likes").value(0));
    find("/api/loadouts/bookmarks",visitor.session()).andExpect(jsonPath("$",hasSize(0)));
  }

  @Test void suggestionsUseSimilarProductsAndExcludeDraftsAndTheCurrentLoadout() throws Exception {
    var owner=register();String name="Mixer "+UUID.randomUUID();
    String source=loadout(owner,"Source",null,false,List.of(Map.of("name",name)));
    String candidate=loadout(owner,"Related",null,false,List.of(Map.of("name",name.toUpperCase(java.util.Locale.ROOT))));
    loadout(owner,"Draft",null,true,List.of(Map.of("name",name)));
    var privateOwner=register();
    loadout(privateOwner,"Private result",null,false,List.of(Map.of("name",name)));
    send(put("/api/profiles/me"),privateOwner.session(),Map.of("privateProfile",true)).andExpect(status().isOk());
    String unrelated=loadout(owner,"Unrelated",null,false,List.of(Map.of("name",UUID.randomUUID().toString())));
    find("/api/loadouts/"+source+"/suggestions",null).andExpect(jsonPath("$[*].id",contains(candidate)));
    find("/api/loadouts/"+unrelated+"/suggestions",null).andExpect(jsonPath("$",hasSize(0)));
  }

  private Account register() throws Exception {
    String username="qa-"+UUID.randomUUID(),email=username+"@example.com";
    var session=new MockHttpSession();
    var body=json(send(post("/api/auth/register"),session,Map.of("email",email,"username",username,"password",PASSWORD)).andExpect(status().isCreated()));
    return new Account(body.path("id").asText(),email,username,session);
  }
  private String loadout(Account account,String title,String category,boolean draft,List<Map<String,Object>> products) throws Exception {
    var body=new java.util.HashMap<String,Object>(Map.of("title",title,"draft",draft,"products",products));
    if(category!=null)body.put("category",category);
    return json(send(post("/api/loadouts"),account.session(),body).andExpect(status().isCreated())).path("id").asText();
  }
  private ResultActions send(MockHttpServletRequestBuilder builder,MockHttpSession s,Object body) throws Exception {
    if(s!=null)builder.session(s);
    return mvc.perform(builder.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body)));
  }
  private ResultActions find(String path,MockHttpSession s) throws Exception {
    var builder=get(path);
    if(s!=null)builder.session(s);
    return mvc.perform(builder);
  }
  private JsonNode json(ResultActions action) throws Exception {
    return mapper.readTree(action.andReturn().getResponse().getContentAsByteArray());
  }
}

