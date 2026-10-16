package com.loadouts.loadout;
import com.loadouts.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="loadouts")
public class Loadout {
  @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @Column(nullable=false,length=120) private String title;
  @Column(length=5000) private String description;
  @Column(length=80) private String category;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="owner_id",nullable=false) private User owner;
  @Column(nullable=false) private Instant createdAt=Instant.now();
  protected Loadout() {}
  public Loadout(String title,String description,String category,User owner){this.title=title;this.description=description;this.category=category;this.owner=owner;}
  public UUID getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;} public String getCategory(){return category;} public User getOwner(){return owner;} public Instant getCreatedAt(){return createdAt;}
}
