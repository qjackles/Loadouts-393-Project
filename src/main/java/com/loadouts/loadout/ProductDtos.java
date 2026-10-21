package com.loadouts.loadout;

import java.util.UUID;

public final class ProductDtos {
  private ProductDtos() {}
  public record CreateProductRequest(String name, String url, String note) {}
  public record ProductResponse(UUID id, String name, String url, String note) {}
}
