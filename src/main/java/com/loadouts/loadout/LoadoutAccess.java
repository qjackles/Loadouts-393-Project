package com.loadouts.loadout;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LoadoutAccess {
  private final LoadoutRepository loadouts;
  public LoadoutAccess(LoadoutRepository loadouts){this.loadouts=loadouts;}
  public static boolean publicRead(Loadout loadout){
    return !loadout.isDraft() && !loadout.getOwner().isPrivateProfile();
  }
  public Loadout readable(UUID id, UUID actor){
    Loadout row=loadouts.findById(id).orElseThrow(()->new LoadoutNotFoundException(id));
    if(!row.getOwner().getId().equals(actor) && !publicRead(row)) throw new LoadoutNotFoundException(id);
    return row;
  }
  public Loadout owned(UUID id,UUID actor){
    if(actor==null) throw new NotLoggedInException();
    Loadout row=loadouts.findForWrite(id).orElseThrow(()->new LoadoutNotFoundException(id));
    if(!row.getOwner().getId().equals(actor)){
      if(!publicRead(row)) throw new LoadoutNotFoundException(id);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the creator can change this loadout.");
    }
    return row;
  }
}
