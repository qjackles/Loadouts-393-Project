package com.loadouts.loadout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
  List<Product> findByLoadoutIdOrderByCreatedAtAsc(UUID loadoutId);
  Optional<Product> findByIdAndLoadoutId(UUID id,UUID loadoutId);
  @Query("select p from Product p where p.loadout.id in :ids")
  List<Product> findForLoadouts(@Param("ids") Collection<UUID> ids);
  @Modifying @Query("delete from Product p where p.loadout.id = :id")
  void deleteForLoadout(@Param("id") UUID id);
}
