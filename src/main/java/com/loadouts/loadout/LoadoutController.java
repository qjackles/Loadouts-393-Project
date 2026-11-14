package com.loadouts.loadout;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import static com.loadouts.auth.AuthController.actor;
import static com.loadouts.loadout.LoadoutDtos.*;

@RestController
@RequestMapping("/api/loadouts")
public class LoadoutController {
  private final LoadoutService service;
  public LoadoutController(LoadoutService service){this.service=service;}
  @PostMapping @ResponseStatus(HttpStatus.CREATED)
  public LoadoutResponse create(@RequestBody CreateLoadoutRequest body,HttpServletRequest request){
    return service.create(actor(request),body);
  }
  @GetMapping public List<LoadoutResponse> getAll(@RequestParam(required=false) String q,@RequestParam(required=false) String category,
      @RequestParam(required=false) BigDecimal minPrice,@RequestParam(required=false) BigDecimal maxPrice,
      @RequestParam(defaultValue="newest") String sort){
    return service.browse(q,category,minPrice,maxPrice,sort);
  }
  @GetMapping("/mine") public List<LoadoutResponse> mine(HttpServletRequest request){return service.mine(actor(request));}
  @GetMapping("/{id}") public LoadoutResponse getById(@PathVariable UUID id,HttpServletRequest request){
    return service.getById(id,actor(request));
  }
  @PutMapping("/{id}") public LoadoutResponse update(@PathVariable UUID id,@RequestBody CreateLoadoutRequest body,HttpServletRequest request){
    return service.update(id,actor(request),body);
  }
  @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id,HttpServletRequest request){service.delete(id,actor(request));}
  @GetMapping("/{id}/suggestions") public List<LoadoutResponse> suggestions(@PathVariable UUID id,HttpServletRequest request){
    return service.suggestions(id,actor(request));
  }
}
