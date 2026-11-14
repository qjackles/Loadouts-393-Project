package com.loadouts.loadout;

import jakarta.persistence.*;
import java.math.BigDecimal;
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
  @Column(length=120) private String store;
  @Column(precision=12, scale=2) private BigDecimal price;
  @Column(length=40) private String availability;
  @Column(nullable=false) private Instant createdAt=Instant.now();

  protected Product() {}
  public Product(Loadout loadout,ProductDtos.CreateProductRequest r){
    this.loadout=loadout; update(r);
  }
  void update(ProductDtos.CreateProductRequest r){
    name=r.name(); url=r.url()==null?"":r.url(); note=r.note();
    store=r.store(); price=r.price(); availability=r.availability();
  }
  public UUID getId(){return id;}
  public Loadout getLoadout(){return loadout;}
  public String getName(){return name;}
  public String getUrl(){return url.isBlank()?null:url;}
  public String getStore(){return store;}
  public BigDecimal getPrice(){return price;}
  public String getAvailability(){return availability;}
  public String getNote(){return note;}
  public Instant getCreatedAt(){return createdAt;}
}
