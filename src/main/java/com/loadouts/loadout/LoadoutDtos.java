package com.loadouts.loadout;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public final class LoadoutDtos {
  private LoadoutDtos() {}
  public record CreateLoadoutRequest(String title,String description,String category,Boolean draft,List<ProductDtos.CreateProductRequest> products) {}
  public record LoadoutResponse(UUID id,String title,String description,String category,UUID ownerId,String ownerUsername,
      Instant createdAt,boolean draft,boolean publiclyVisible,BigDecimal knownTotal,int pricedCount,int missingPriceCount) {}
}
