package com.loadouts.loadout;

import com.loadouts.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="loadout_interactions",uniqueConstraints=@UniqueConstraint(columnNames={"loadout_id","actor_id","kind","unique_slot"}))
public class Interaction {
  enum Kind { LIKE, BOOKMARK, COMMENT, REVIEW }
  @Id @GeneratedValue(strategy=GenerationType.UUID) UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="loadout_id") Loadout loadout;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="actor_id") User actor;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) Kind kind;
  @Column(name="unique_slot",nullable=false,length=36) String slot;
  @Column(length=3000) String body;
  @Column(nullable=false) Instant createdAt=Instant.now();
  protected Interaction(){}
  Interaction(Loadout loadout,User actor,Kind kind,String body){
    this.loadout=loadout;this.actor=actor;this.kind=kind;this.body=body;
    this.slot=(kind==Kind.LIKE || kind==Kind.BOOKMARK)?"toggle":UUID.randomUUID().toString();
  }
}
