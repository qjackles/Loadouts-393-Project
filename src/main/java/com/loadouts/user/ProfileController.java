package com.loadouts.user;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.loadouts.loadout.LoadoutService;
import com.loadouts.loadout.NotLoggedInException;
import com.loadouts.loadout.ProductService;
import com.loadouts.loadout.LoadoutDtos.LoadoutResponse;
import static com.loadouts.auth.AuthController.actor;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
  private final UserRepository users;
  private final LoadoutService loadouts;
  public ProfileController(UserRepository users,LoadoutService loadouts){this.users=users;this.loadouts=loadouts;}
  public record Profile(UUID id,String username,String displayName,String bio,boolean privateProfile,List<LoadoutResponse> loadouts){}
  public record Update(String displayName,String bio,Boolean privateProfile){}
  @Transactional(readOnly=true) @GetMapping("/{id}")
  public Profile read(@PathVariable UUID id,HttpServletRequest request){
    var user=users.findById(id).orElseThrow(ProfileController::missing);
    if(user.isPrivateProfile() && !id.equals(actor(request))) throw missing();
    return profile(user);
  }
  @Transactional @PutMapping("/me")
  public Profile update(@RequestBody Update body,HttpServletRequest request){
    UUID id=actor(request);
    if(id==null) throw new NotLoggedInException();
    var user=users.findById(id).orElseThrow(NotLoggedInException::new);
    user.updateProfile(ProductService.optional(body.displayName(),"Display name",80),
        ProductService.optional(body.bio(),"Bio",1000),
        body.privateProfile()==null?user.isPrivateProfile():body.privateProfile());
    return profile(user);
  }
  private Profile profile(User user){
    var published=user.isPrivateProfile()?loadouts.mine(user.getId()).stream().filter(l->!l.draft()).toList():loadouts.publicByOwner(user.getId());
    return new Profile(user.getId(),user.getUsername(),user.getDisplayName(),user.getBio(),user.isPrivateProfile(),published);
  }
  private static ResponseStatusException missing(){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Profile not found.");}
}
