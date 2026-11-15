package com.loadouts.loadout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface LoadoutRepository extends JpaRepository<Loadout,UUID>{
  @Query("select l from Loadout l join fetch l.owner u where (l.draft is null or l.draft=false) and (u.privateProfile is null or u.privateProfile=false)")
  List<Loadout> findPublic();
  @EntityGraph(attributePaths="owner")
  List<Loadout> findByOwnerIdOrderByCreatedAtDescIdAsc(UUID id);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select l from Loadout l where l.id = :id")
  Optional<Loadout> findForWrite(@Param("id") UUID id);
}
