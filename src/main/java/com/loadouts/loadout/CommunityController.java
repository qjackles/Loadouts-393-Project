package com.loadouts.loadout;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import static com.loadouts.auth.AuthController.actor;
import static com.loadouts.loadout.Interaction.Kind.*;

@RestController
@RequestMapping("/api/loadouts")
public class CommunityController {
  private final CommunityService service;
  public CommunityController(CommunityService service){this.service=service;}
  @GetMapping("/bookmarks") public List<LoadoutDtos.LoadoutResponse> bookmarks(HttpServletRequest request){
    return service.bookmarks(actor(request));
  }
  @GetMapping("/{id}/community") public CommunityService.Summary read(@PathVariable UUID id,HttpServletRequest request){
    return service.read(id,actor(request));
  }
  @PutMapping("/{id}/like") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void like(@PathVariable UUID id,HttpServletRequest request){service.toggle(id,actor(request),LIKE,true);}
  @DeleteMapping("/{id}/like") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unlike(@PathVariable UUID id,HttpServletRequest request){service.toggle(id,actor(request),LIKE,false);}
  @PutMapping("/{id}/bookmark") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void mark(@PathVariable UUID id,HttpServletRequest request){service.toggle(id,actor(request),BOOKMARK,true);}
  @DeleteMapping("/{id}/bookmark") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unmark(@PathVariable UUID id,HttpServletRequest request){service.toggle(id,actor(request),BOOKMARK,false);}
  @PostMapping("/{id}/comments") @ResponseStatus(HttpStatus.CREATED)
  public CommunityService.Feedback comment(@PathVariable UUID id,@RequestBody Body body,HttpServletRequest request){
    return service.add(id,actor(request),COMMENT,body.body());
  }
  @PostMapping("/{id}/reviews") @ResponseStatus(HttpStatus.CREATED)
  public CommunityService.Feedback review(@PathVariable UUID id,@RequestBody Body body,HttpServletRequest request){
    return service.add(id,actor(request),REVIEW,body.body());
  }
  public record Body(String body){}
}
