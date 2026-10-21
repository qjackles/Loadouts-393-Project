package com.loadouts.loadout;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="products")
public class Product {
  @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="loadout_id", nullable=false) private Loadout loadout;
  @Column(nullable=false, length=120) private String name;
  @Column(nullable=false, length=2048) private String url;
  @Column(length=1000) private String note;
  @Column(nullable=false) private Instant createdAt=Instant.now();

  protected Product() {}
  public Product(Loadout loadout, String name, String url, String note) {
    this.loadout=loadout; this.name=name; this.url=url; this.note=note;
  }
  public UUID getId(){return id;}
  public Loadout getLoadout(){return loadout;}
  public String getName(){return name;}
  public String getUrl(){return url;}
  public String getNote(){return note;}
  public Instant getCreatedAt(){return createdAt;}
}
