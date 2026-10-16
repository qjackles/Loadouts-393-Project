package com.loadouts.loadout;
import com.loadouts.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static com.loadouts.loadout.LoadoutDtos.*;
@Service
public class LoadoutService {
  private final LoadoutRepository loadouts; private final UserRepository users;
  public LoadoutService(LoadoutRepository loadouts,UserRepository users){this.loadouts=loadouts;this.users=users;}
  @Transactional public LoadoutResponse create(UUID userId,CreateLoadoutRequest r){
    if(r.title()==null||r.title().isBlank()) throw new IllegalArgumentException("title is required.");
    User owner=users.findById(userId).orElseThrow(()->new IllegalArgumentException("User not found."));
    return map(loadouts.save(new Loadout(r.title().trim(),r.description(),r.category(),owner)));
  }
  @Transactional(readOnly=true) public List<LoadoutResponse> getAll(){return loadouts.findAllByOrderByCreatedAtDesc().stream().map(this::map).toList();}
  @Transactional(readOnly=true) public LoadoutResponse getById(UUID id){return map(loadouts.findById(id).orElseThrow(()->new LoadoutNotFoundException(id)));}
  private LoadoutResponse map(Loadout l){return new LoadoutResponse(l.getId(),l.getTitle(),l.getDescription(),l.getCategory(),l.getOwner().getId(),l.getOwner().getUsername(),l.getCreatedAt());}
}
