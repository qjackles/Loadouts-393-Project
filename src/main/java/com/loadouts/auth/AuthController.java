package com.loadouts.auth;
import com.loadouts.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import static com.loadouts.auth.AuthDtos.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
  public static final String SESSION_USER_ID="userId"; private final AuthService service;
  public AuthController(AuthService service){this.service=service;}
  @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@RequestBody RegisterRequest r,HttpSession s){UserResponse u=service.register(r);s.setAttribute(SESSION_USER_ID,u.id());return u;}
  @PostMapping("/login") public UserResponse login(@RequestBody LoginRequest r,HttpSession s){User u=service.authenticate(r);s.setAttribute(SESSION_USER_ID,u.getId());return AuthService.toResponse(u);}
  @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) public void logout(HttpSession s){s.invalidate();}
}
