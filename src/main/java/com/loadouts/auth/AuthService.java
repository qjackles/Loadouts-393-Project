package com.loadouts.auth;
import com.loadouts.user.*;
import com.loadouts.loadout.NotLoggedInException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.loadouts.auth.AuthDtos.*;
@Service
public class AuthService {
  private final UserRepository users; private final PasswordEncoder encoder;
  private final String dummyHash;
  public AuthService(UserRepository users, PasswordEncoder encoder){
    this.users=users;this.encoder=encoder;
    this.dummyHash=encoder.encode(UUID.randomUUID().toString());
  }
  @Transactional public UserResponse register(RegisterRequest r){
    String email=req(r.email(),"email").trim().toLowerCase(Locale.ROOT); String username=req(r.username(),"username").trim().toLowerCase(Locale.ROOT); String password=req(r.password(),"password");
    if(password.length()<8) throw new IllegalArgumentException("Password must be at least 8 characters.");
    if(password.getBytes(StandardCharsets.UTF_8).length>72) throw new IllegalArgumentException("Password must be at most 72 UTF-8 bytes.");
    if(email.length()>255 || username.length()>255) throw new IllegalArgumentException("Email and username must be at most 255 characters.");
    if(users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Email is already registered.");
    if(users.existsByUsernameIgnoreCase(username)) throw new IllegalArgumentException("Username is already taken.");
    return toResponse(users.saveAndFlush(new User(email,username,encoder.encode(password))));
  }
  public User authenticate(LoginRequest r){
    String email=req(r.email(),"email").trim().toLowerCase(Locale.ROOT); String password=req(r.password(),"password");
    User u=users.findByEmailIgnoreCase(email).orElse(null);
    if(password.getBytes(StandardCharsets.UTF_8).length>72
        || !encoder.matches(password,u==null?dummyHash:u.getPasswordHash()) || u==null)
      throw new IllegalArgumentException("Invalid email or password.");
    return u;
  }
  public UserResponse current(UUID id){
    if(id==null) throw new NotLoggedInException();
    return users.findById(id).map(AuthService::toResponse).orElseThrow(NotLoggedInException::new);
  }
  public static UserResponse toResponse(User u){return new UserResponse(u.getId(),u.getEmail(),u.getUsername());}
  private static String req(String v,String f){if(v==null||v.isBlank()) throw new IllegalArgumentException(f+" is required."); return v;}
}
