package com.loadouts.loadout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static com.loadouts.loadout.ProductDtos.*;

@Service
public class ProductService {
  private final ProductRepository products;
  private final LoadoutAccess access;
  public ProductService(ProductRepository products,LoadoutAccess access){
    this.products=products;this.access=access;
  }
  @Transactional(readOnly=true)
  public List<ProductResponse> list(UUID id,UUID actor){
    access.readable(id,actor);
    return products.findByLoadoutIdOrderByCreatedAtAsc(id).stream().map(ProductService::response).toList();
  }
  @Transactional
  public ProductResponse create(UUID id,UUID actor,CreateProductRequest request){
    return add(access.owned(id,actor),request);
  }
  ProductResponse add(Loadout loadout,CreateProductRequest request){
    return response(products.save(new Product(loadout,validated(request))));
  }
  @Transactional
  public ProductResponse update(UUID id,UUID productId,UUID actor,CreateProductRequest request){
    access.owned(id,actor);
    Product product=find(id,productId);
    product.update(validated(request));
    return response(product);
  }
  @Transactional
  public void delete(UUID id,UUID productId,UUID actor){
    access.owned(id,actor);
    products.delete(find(id,productId));
  }
  private Product find(UUID id,UUID productId){
    return products.findByIdAndLoadoutId(productId,id)
        .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found."));
  }
  private static CreateProductRequest validated(CreateProductRequest r){
    if(r==null) throw new IllegalArgumentException("Product details are required.");
    String name=optional(r.name(),"Product name",120);
    if(name==null) throw new IllegalArgumentException("Product name is required.");
    String url=optional(r.url(),"Product link",2048);
    if(url!=null){
      try{
        URI uri=URI.create(url);
        if(uri.getScheme()==null || !Set.of("http","https").contains(uri.getScheme().toLowerCase(Locale.ROOT))
            || uri.getHost()==null || uri.getUserInfo()!=null) throw new IllegalArgumentException();
      }catch(IllegalArgumentException ex){throw new IllegalArgumentException("Product link must be a valid http or https URL.");}
    }
    BigDecimal price=r.price();
    if(price!=null){
      if(price.signum()<0 || price.compareTo(new BigDecimal("9999999999.99"))>0 || price.stripTrailingZeros().scale()>2)
        throw new IllegalArgumentException("Enter a nonnegative USD price with at most two decimal places.");
      price=price.setScale(2,RoundingMode.UNNECESSARY);
    }
    String availability=optional(r.availability(),"Availability",40);
    if(availability!=null && !Set.of("In stock","Out of stock").contains(availability))
      throw new IllegalArgumentException("Unsupported availability.");
    return new CreateProductRequest(name,url,optional(r.note(),"Product note",1000),
        optional(r.store(),"Storefront",120),price,availability);
  }
  public static String optional(String value,String label,int limit){
    if(value==null || value.isBlank()) return null;
    String trimmed=value.strip();
    if(trimmed.length()>limit) throw new IllegalArgumentException(label+" must be "+limit+" characters or fewer.");
    return trimmed;
  }
  private static ProductResponse response(Product p){
    return new ProductResponse(p.getId(),p.getName(),p.getUrl(),p.getNote(),p.getStore(),p.getPrice(),p.getAvailability());
  }
}
