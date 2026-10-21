package com.loadouts.loadout;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static com.loadouts.loadout.ProductDtos.*;

@Service
public class ProductService {
  private final ProductRepository products;
  private final LoadoutRepository loadouts;

  public ProductService(ProductRepository products, LoadoutRepository loadouts) {
    this.products=products; this.loadouts=loadouts;
  }

  @Transactional(readOnly=true)
  public List<ProductResponse> list(UUID loadoutId) {
    findLoadout(loadoutId);
    return products.findByLoadoutIdOrderByCreatedAtAsc(loadoutId).stream().map(ProductService::response).toList();
  }

  @Transactional
  public ProductResponse create(UUID loadoutId, UUID userId, CreateProductRequest request) {
    Loadout loadout=findLoadout(loadoutId);
    requireOwner(loadout, userId);
    String name=required(request.name(), "Product name", 120);
    String url=required(request.url(), "Product link", 2048);
    try {
      URI uri=URI.create(url);
      if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
          || uri.getHost()==null || uri.getHost().isBlank() || uri.getUserInfo()!=null)
        throw new IllegalArgumentException("Product link must be a valid http or https URL.");
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Product link must be a valid http or https URL.");
    }
    String note=request.note()==null?null:request.note().trim();
    if (note!=null && note.length()>1000) throw new IllegalArgumentException("Product note must be 1000 characters or fewer.");
    return response(products.save(new Product(loadout, name, url, note)));
  }

  @Transactional
  public void delete(UUID loadoutId, UUID productId, UUID userId) {
    Loadout loadout=findLoadout(loadoutId);
    requireOwner(loadout, userId);
    Product product=products.findById(productId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));
    if (!product.getLoadout().getId().equals(loadoutId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
    products.delete(product);
  }

  private Loadout findLoadout(UUID id) {
    return loadouts.findById(id).orElseThrow(()->new LoadoutNotFoundException(id));
  }
  private static void requireOwner(Loadout loadout, UUID userId) {
    if (userId==null) throw new NotLoggedInException();
    if (!loadout.getOwner().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator can change products.");
  }
  private static String required(String value, String label, int maxLength) {
    if (value==null || value.isBlank()) throw new IllegalArgumentException(label+" is required.");
    String trimmed=value.trim();
    if (trimmed.length()>maxLength) throw new IllegalArgumentException(label+" must be "+maxLength+" characters or fewer.");
    return trimmed;
  }
  private static ProductResponse response(Product product) {
    return new ProductResponse(product.getId(), product.getName(), product.getUrl(), product.getNote());
  }
}
