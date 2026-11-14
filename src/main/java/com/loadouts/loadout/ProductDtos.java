package com.loadouts.loadout;

import java.util.UUID;
import java.math.BigDecimal;

public final class ProductDtos {
  private ProductDtos() {}
  public record CreateProductRequest(String name,String url,String note,String store,BigDecimal price,String availability) {}
  public record ProductResponse(UUID id,String name,String url,String note,String store,BigDecimal price,String availability) {}
}
