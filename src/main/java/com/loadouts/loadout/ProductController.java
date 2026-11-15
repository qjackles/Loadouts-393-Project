package com.loadouts.loadout;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import static com.loadouts.auth.AuthController.actor;
import static com.loadouts.loadout.ProductDtos.*;

@RestController
@RequestMapping("/api/loadouts/{loadoutId}/products")
public class ProductController {
  private final ProductService service;
  public ProductController(ProductService service){this.service=service;}
  @GetMapping public List<ProductResponse> list(@PathVariable UUID loadoutId,HttpServletRequest request){
    return service.list(loadoutId,actor(request));
  }
  @PostMapping @ResponseStatus(HttpStatus.CREATED)
  public ProductResponse create(@PathVariable UUID loadoutId,@RequestBody CreateProductRequest body,HttpServletRequest request){
    return service.create(loadoutId,actor(request),body);
  }
  @PutMapping("/{productId}")
  public ProductResponse update(@PathVariable UUID loadoutId,@PathVariable UUID productId,
      @RequestBody CreateProductRequest body,HttpServletRequest request){
    return service.update(loadoutId,productId,actor(request),body);
  }
  @DeleteMapping("/{productId}") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID loadoutId,@PathVariable UUID productId,HttpServletRequest request){
    service.delete(loadoutId,productId,actor(request));
  }
}
