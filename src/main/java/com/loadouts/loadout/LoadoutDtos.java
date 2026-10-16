package com.loadouts.loadout;
import java.time.Instant;
import java.util.UUID;
public final class LoadoutDtos {
  private LoadoutDtos() {}
  public record CreateLoadoutRequest(String title,String description,String category) {}
  public record LoadoutResponse(UUID id,String title,String description,String category,UUID ownerId,String ownerUsername,Instant createdAt) {}
}
