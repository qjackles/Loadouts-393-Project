package com.loadouts.auth;
import com.loadouts.user.User;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import static com.loadouts.auth.AuthDtos.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
  public static final String SESSION_USER_ID="userId"; private final AuthService service;
  public AuthController(AuthService service){this.service=service;}
  @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@RequestBody RegisterRequest r,HttpServletRequest request){
    UserResponse u=service.register(r); establish(request,u.id()); return u;
  }
  @PostMapping("/login") public UserResponse login(@RequestBody LoginRequest r,HttpServletRequest request){
    User u=service.authenticate(r); establish(request,u.getId()); return AuthService.toResponse(u);
  }
  @GetMapping("/me") public ResponseEntity<UserResponse> me(HttpServletRequest request){
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.current(actor(request)));
  }
  @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) public void logout(HttpServletRequest request){
    HttpSession session=request.getSession(false); if(session!=null) session.invalidate();
  }
  private static void establish(HttpServletRequest request, java.util.UUID id){
    HttpSession session=request.getSession();
    request.changeSessionId();
    session.setAttribute(SESSION_USER_ID,id);
  }
  public static java.util.UUID actor(HttpServletRequest request){
    HttpSession session=request.getSession(false);
    return session==null?null:(java.util.UUID)session.getAttribute(SESSION_USER_ID);
  }
}
