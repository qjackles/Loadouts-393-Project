package com.loadouts.loadout;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.loadouts.loadout.Interaction.Kind.*;

@Service
public class CommunityService {
  private final InteractionRepository entries;
  private final LoadoutRepository repo;
  private final LoadoutAccess access;
  private final LoadoutService loadouts;
  public CommunityService(InteractionRepository entries,LoadoutRepository repo,LoadoutAccess access,LoadoutService loadouts){
    this.entries=entries;this.repo=repo;this.access=access;this.loadouts=loadouts;
  }
  public record Feedback(UUID id,String username,String body,Instant createdAt){}
  public record Summary(long likes,boolean liked,boolean bookmarked,List<Feedback> comments,List<Feedback> reviews){}
  private static Feedback feedback(Interaction i){return new Feedback(i.id,i.actor.getUsername(),i.body,i.createdAt);}
  @Transactional(readOnly=true) public Summary read(UUID id,UUID actor){
    if(!LoadoutAccess.publicRead(access.readable(id,actor))) return new Summary(0,false,false,List.of(),List.of());
    long likes=0; boolean liked=false, bookmarked=false;
    List<Feedback> comments=new ArrayList<>(),reviews=new ArrayList<>();
    for(Interaction interaction:entries.findByLoadoutIdOrderByCreatedAtAscIdAsc(id)){
      boolean own=interaction.actor.getId().equals(actor);
      switch(interaction.kind){
        case LIKE -> {likes++;liked|=own;}
        case BOOKMARK -> bookmarked|=own;
        case COMMENT -> comments.add(feedback(interaction));
        case REVIEW -> reviews.add(feedback(interaction));
      }
    }
    return new Summary(likes,liked,bookmarked,comments,reviews);
  }
  @Transactional public void toggle(UUID id,UUID actor,Interaction.Kind kind,boolean enabled){
    var user=loadouts.user(actor);
    var row=publicForWrite(id);
    if(enabled){
      if(!entries.existsByLoadoutIdAndActorIdAndKind(id,actor,kind))
        entries.save(new Interaction(row,user,kind,null));
    }else entries.deleteByLoadoutIdAndActorIdAndKind(id,actor,kind);
  }
  @Transactional public Feedback add(UUID id,UUID actor,Interaction.Kind kind,String input){
    var user=loadouts.user(actor);
    var row=publicForWrite(id);
    String body=ProductService.optional(input,"Feedback",3000);
    if(body==null) throw new IllegalArgumentException("Enter feedback before submitting.");
    return feedback(entries.save(new Interaction(row,user,kind,body)));
  }
  @Transactional(readOnly=true) public List<LoadoutDtos.LoadoutResponse> bookmarks(UUID actor){
    loadouts.user(actor);
    return loadouts.summaries(entries.findByActorIdAndKindOrderByCreatedAtDesc(actor,BOOKMARK).stream()
        .map(e->e.loadout).filter(LoadoutAccess::publicRead).toList());
  }
  private Loadout publicForWrite(UUID id){
    var row=repo.findForWrite(id).orElseThrow(()->new LoadoutNotFoundException(id));
    if(!LoadoutAccess.publicRead(row)) throw new LoadoutNotFoundException(id);
    return row;
  }
}
