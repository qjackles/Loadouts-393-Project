package com.loadouts.auth;
import java.util.UUID;
public final class AuthDtos {
  private AuthDtos() {}
  public record RegisterRequest(String email, String username, String password) {}
  public record LoginRequest(String email, String password) {}
  public record UserResponse(UUID id, String email, String username) {}
}
