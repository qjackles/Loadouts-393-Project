package com.loadouts.common;
import com.loadouts.loadout.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import java.util.Map;
@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST) public Map<String,String> bad(IllegalArgumentException e){return Map.of("error",e.getMessage());}
  @ExceptionHandler(NotLoggedInException.class) @ResponseStatus(HttpStatus.UNAUTHORIZED) public Map<String,String> unauth(NotLoggedInException e){return Map.of("error",e.getMessage());}
  @ExceptionHandler(LoadoutNotFoundException.class) @ResponseStatus(HttpStatus.NOT_FOUND) public Map<String,String> missing(LoadoutNotFoundException e){return Map.of("error",e.getMessage());}
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String,String>> status(ResponseStatusException e){
    return ResponseEntity.status(e.getStatusCode()).body(Map.of("error",e.getReason()==null?"Request rejected.":e.getReason()));
  }
  @ExceptionHandler(DataIntegrityViolationException.class) @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String,String> conflict(){return Map.of("error","The change conflicts with existing data. Reload and try again.");}
}
