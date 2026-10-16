package com.loadouts.loadout;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LoadoutRepository extends JpaRepository<Loadout,UUID>{ List<Loadout> findAllByOrderByCreatedAtDesc(); }
