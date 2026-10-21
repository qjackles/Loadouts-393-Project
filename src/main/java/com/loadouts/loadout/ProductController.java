package com.loadouts.loadout;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import static com.loadouts.auth.AuthController.SESSION_USER_ID;
import static com.loadouts.loadout.ProductDtos.*;

@RestController
@RequestMapping("/api/loadouts/{loadoutId}/products")
public class ProductController {
  private final ProductService service;
  public ProductController(ProductService service) { this.service=service; }

  @GetMapping public List<ProductResponse> list(@PathVariable UUID loadoutId) {return service.list(loadoutId);}
  @PostMapping @ResponseStatus(HttpStatus.CREATED)
  public ProductResponse create(@PathVariable UUID loadoutId, @RequestBody CreateProductRequest request, HttpSession session) {
    return service.create(loadoutId, (UUID)session.getAttribute(SESSION_USER_ID), request);
  }
  @DeleteMapping("/{productId}") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID loadoutId, @PathVariable UUID productId, HttpSession session) {
    service.delete(loadoutId, productId, (UUID)session.getAttribute(SESSION_USER_ID));
  }
}
