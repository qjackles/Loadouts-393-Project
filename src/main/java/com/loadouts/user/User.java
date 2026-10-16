package com.loadouts.user;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name="users", uniqueConstraints={@UniqueConstraint(columnNames="email"), @UniqueConstraint(columnNames="username")})
public class User {
  @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @Column(nullable=false) private String email;
  @Column(nullable=false) private String username;
  @Column(name="password_hash", nullable=false) private String passwordHash;
  @Column(nullable=false) private Instant createdAt = Instant.now();
  protected User() {}
  public User(String email, String username, String passwordHash) { this.email=email; this.username=username; this.passwordHash=passwordHash; }
  public UUID getId(){return id;} public String getEmail(){return email;} public String getUsername(){return username;} public String getPasswordHash(){return passwordHash;} public Instant getCreatedAt(){return createdAt;}
}
