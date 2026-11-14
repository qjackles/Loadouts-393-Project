package com.loadouts.loadout;

import com.loadouts.user.User;
import com.loadouts.user.UserRepository;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.loadouts.loadout.LoadoutDtos.*;
import static com.loadouts.loadout.ProductService.optional;

@Service
public class LoadoutService {
  private static final Set<String> CATEGORIES=Set.of("Cooking","Outdoors","Technology","Photography","Fitness","Other");
  private final LoadoutRepository loadouts;
  private final UserRepository users;
  private final ProductRepository products;
  private final ProductService entry;
  private final InteractionRepository interactions;
  private final LoadoutAccess access;
  public LoadoutService(LoadoutRepository loadouts,UserRepository users,ProductRepository products,
      ProductService entry,InteractionRepository interactions,LoadoutAccess access){
    this.loadouts=loadouts;this.users=users;this.products=products;this.entry=entry;
    this.interactions=interactions;this.access=access;
  }
  @Transactional public LoadoutResponse create(UUID actor,CreateLoadoutRequest r){
    User owner=user(actor);
    Loadout row=new Loadout(null,null,null,owner);
    apply(row,r,false);
    loadouts.save(row);
    if(r.products()!=null){
      if(r.products().size()>50) throw new IllegalArgumentException("Add at most 50 products at a time.");
      for(var product:r.products()) entry.add(row,product);
    }
    return map(row,products.findByLoadoutIdOrderByCreatedAtAsc(row.getId()));
  }
  @Transactional public LoadoutResponse update(UUID id,UUID actor,CreateLoadoutRequest r){
    Loadout row=access.owned(id,actor);
    if(r.products()!=null) throw new IllegalArgumentException("Edit saved products using their product controls.");
    apply(row,r,row.isDraft());
    return map(row,products.findByLoadoutIdOrderByCreatedAtAsc(id));
  }
  @Transactional public void delete(UUID id,UUID actor){
    Loadout row=access.owned(id,actor);
    interactions.deleteForLoadout(id);
    products.deleteForLoadout(id);
    loadouts.delete(row);
  }
  private static void apply(Loadout row,CreateLoadoutRequest r,boolean fallbackDraft){
    String title=optional(r.title(),"Title",120);
    if(title==null) throw new IllegalArgumentException("title is required.");
    String category=optional(r.category(),"Category",80);
    if(category!=null && !CATEGORIES.contains(category) && !category.equals(row.getCategory()))
      throw new IllegalArgumentException("Choose a supported category.");
    row.update(title,optional(r.description(),"Description",5000),category,r.draft()==null?fallbackDraft:r.draft());
  }
  @Transactional(readOnly=true) public LoadoutResponse getById(UUID id,UUID actor){
    return map(access.readable(id,actor),products.findByLoadoutIdOrderByCreatedAtAsc(id));
  }
  @Transactional(readOnly=true) public List<LoadoutResponse> mine(UUID actor){
    user(actor);
    return summaries(loadouts.findByOwnerIdOrderByCreatedAtDescIdAsc(actor));
  }
  @Transactional(readOnly=true) public List<LoadoutResponse> publicByOwner(UUID id){
    return summaries(loadouts.findByOwnerIdOrderByCreatedAtDescIdAsc(id).stream().filter(LoadoutAccess::publicRead).toList());
  }
  @Transactional(readOnly=true) public List<LoadoutResponse> browse(String q,String category,BigDecimal min,BigDecimal max,String sort){
    String needle=normalize(optional(q,"Search",200));
    if((min!=null && min.signum()<0) || (max!=null && max.signum()<0) || (min!=null && max!=null && min.compareTo(max)>0))
      throw new IllegalArgumentException("Enter a valid price range.");
    Comparator<LoadoutResponse> order=switch(sort){
      case "newest" -> Comparator.comparing(LoadoutResponse::createdAt).reversed();
      case "oldest" -> Comparator.comparing(LoadoutResponse::createdAt);
      case "title" -> Comparator.comparing(LoadoutResponse::title,String.CASE_INSENSITIVE_ORDER);
      case "price-low" -> Comparator.comparing(LoadoutResponse::knownTotal,Comparator.nullsLast(Comparator.naturalOrder()));
      case "price-high" -> Comparator.comparing(LoadoutResponse::knownTotal,Comparator.nullsLast(Comparator.reverseOrder()));
      default -> throw new IllegalArgumentException("Choose a supported sort order.");
    };
    var matching=loadouts.findPublic().stream()
      .filter(l->category==null || category.isBlank() || normalize(l.getCategory()).equals(normalize(category)))
      .filter(l->needle.isEmpty() || normalize(l.getTitle()).contains(needle) || normalize(l.getDescription()).contains(needle)
                   || normalize(l.getCategory()).contains(needle) || normalize(l.getOwner().getUsername()).contains(needle))
      .toList();
    return summaries(matching).stream()
      .filter(l->(min==null && max==null) || l.knownTotal()!=null
          && (min==null || l.knownTotal().compareTo(min)>=0) && (max==null || l.knownTotal().compareTo(max)<=0))
      .sorted(order.thenComparing(LoadoutResponse::id)).toList();
  }
  @Transactional(readOnly=true) public List<LoadoutResponse> suggestions(UUID id,UUID actor){
    Loadout row=access.readable(id,actor);
    if(!LoadoutAccess.publicRead(row)) return List.of();
    List<Loadout> candidates=loadouts.findPublic().stream().filter(l->!id.equals(l.getId())).toList();
    Map<UUID,List<Product>> entries=groupProducts(candidates);
    List<Product> source=products.findByLoadoutIdOrderByCreatedAtAsc(id);
    Set<String> names=names(source);
    LoadoutResponse current=map(row,source);
    record Ranked(LoadoutResponse summary,double score){}
    return candidates.stream().map(candidate->{
      var parts=entries.getOrDefault(candidate.getId(),List.of());
      var summary=map(candidate,parts);
      var other=names(parts);
      long shared=other.stream().filter(names::contains).count();
      var category=normalize(row.getCategory());
      double score=!category.isEmpty() && category.equals(normalize(candidate.getCategory()))?3:0;
      if(shared>0) score+=2.0*shared/(names.size()+other.size()-shared);
      if(score>0 && fullyPriced(current) && fullyPriced(summary)){
        double a=current.knownTotal().doubleValue(),b=summary.knownTotal().doubleValue();
        score+=Math.max(0,1-Math.abs(a-b)/Math.max(1,Math.max(a,b)));
      }
      return new Ranked(summary,score);
    }).filter(r->r.score()>0).sorted(Comparator.comparingDouble(Ranked::score).reversed()
        .thenComparing(r->r.summary().createdAt(),Comparator.reverseOrder())
        .thenComparing(r->r.summary().id())).limit(4).map(Ranked::summary).toList();
  }
  private static boolean fullyPriced(LoadoutResponse l){return l.pricedCount()>0 && l.missingPriceCount()==0;}
  private static Set<String> names(List<Product> p){
    return p.stream().map(x->normalize(x.getName()).replaceAll("\\s+"," ")).collect(Collectors.toSet());
  }
  List<LoadoutResponse> summaries(List<Loadout> rows){
    var grouped=groupProducts(rows);
    return rows.stream().map(l->map(l,grouped.getOrDefault(l.getId(),List.of()))).toList();
  }
  private Map<UUID,List<Product>> groupProducts(List<Loadout> rows){
    if(rows.isEmpty()) return Map.of();
    return products.findForLoadouts(rows.stream().map(Loadout::getId).toList()).stream()
        .collect(Collectors.groupingBy(p->p.getLoadout().getId()));
  }
  private static LoadoutResponse map(Loadout l,List<Product> parts){
    var priced=parts.stream().map(Product::getPrice).filter(Objects::nonNull).toList();
    BigDecimal total=priced.isEmpty()?null:priced.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
    return new LoadoutResponse(l.getId(),l.getTitle(),l.getDescription(),l.getCategory(),l.getOwner().getId(),
        l.getOwner().getUsername(),l.getCreatedAt(),l.isDraft(),LoadoutAccess.publicRead(l),total,priced.size(),parts.size()-priced.size());
  }
  User user(UUID actor){
    if(actor==null) throw new NotLoggedInException();
    return users.findById(actor).orElseThrow(NotLoggedInException::new);
  }
  private static String normalize(String value){return value==null?"":value.strip().toLowerCase(Locale.ROOT);}
}
