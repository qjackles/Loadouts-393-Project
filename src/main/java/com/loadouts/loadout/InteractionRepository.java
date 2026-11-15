package com.loadouts.loadout;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface InteractionRepository extends JpaRepository<Interaction,UUID> {
  @EntityGraph(attributePaths="actor")
  List<Interaction> findByLoadoutIdOrderByCreatedAtAscIdAsc(UUID id);
  List<Interaction> findByActorIdAndKindOrderByCreatedAtDesc(UUID id,Interaction.Kind kind);
  boolean existsByLoadoutIdAndActorIdAndKind(UUID loadout,UUID actor,Interaction.Kind kind);
  void deleteByLoadoutIdAndActorIdAndKind(UUID loadout,UUID actor,Interaction.Kind kind);
  @Modifying @Query("delete from Interaction i where i.loadout.id=:id")
  void deleteForLoadout(@Param("id") UUID id);
}
