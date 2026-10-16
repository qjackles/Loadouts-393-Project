package com.loadouts.auth;
import com.loadouts.user.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.loadouts.auth.AuthDtos.*;
@Service
public class AuthService {
  private final UserRepository users; private final PasswordEncoder encoder;
  public AuthService(UserRepository users, PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
  @Transactional public UserResponse register(RegisterRequest r){
    String email=req(r.email(),"email").trim().toLowerCase(); String username=req(r.username(),"username").trim(); String password=req(r.password(),"password");
    if(password.length()<8) throw new IllegalArgumentException("Password must be at least 8 characters.");
    if(users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Email is already registered.");
    if(users.existsByUsernameIgnoreCase(username)) throw new IllegalArgumentException("Username is already taken.");
    return toResponse(users.save(new User(email,username,encoder.encode(password))));
  }
  public User authenticate(LoginRequest r){
    String email=req(r.email(),"email").trim().toLowerCase(); String password=req(r.password(),"password");
    User u=users.findByEmailIgnoreCase(email).orElseThrow(()->new IllegalArgumentException("Invalid email or password."));
    if(!encoder.matches(password,u.getPasswordHash())) throw new IllegalArgumentException("Invalid email or password."); return u;
  }
  public static UserResponse toResponse(User u){return new UserResponse(u.getId(),u.getEmail(),u.getUsername());}
  private static String req(String v,String f){if(v==null||v.isBlank()) throw new IllegalArgumentException(f+" is required."); return v;}
}
