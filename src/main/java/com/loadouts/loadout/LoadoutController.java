package com.loadouts.loadout;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static com.loadouts.auth.AuthController.SESSION_USER_ID;
import static com.loadouts.loadout.LoadoutDtos.*;
@RestController @RequestMapping("/api/loadouts")
public class LoadoutController {
  private final LoadoutService service; public LoadoutController(LoadoutService service){this.service=service;}
  @PostMapping @ResponseStatus(HttpStatus.CREATED)
  public LoadoutResponse create(@RequestBody CreateLoadoutRequest r,HttpSession s){UUID uid=(UUID)s.getAttribute(SESSION_USER_ID);if(uid==null)throw new NotLoggedInException();return service.create(uid,r);}
  @GetMapping public List<LoadoutResponse> getAll(){return service.getAll();}
  @GetMapping("/{id}") public LoadoutResponse getById(@PathVariable UUID id){return service.getById(id);}
}
