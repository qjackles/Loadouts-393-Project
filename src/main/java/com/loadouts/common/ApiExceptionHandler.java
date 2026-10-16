package com.loadouts.common;
import com.loadouts.loadout.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST) public Map<String,String> bad(IllegalArgumentException e){return Map.of("error",e.getMessage());}
  @ExceptionHandler(NotLoggedInException.class) @ResponseStatus(HttpStatus.UNAUTHORIZED) public Map<String,String> unauth(NotLoggedInException e){return Map.of("error",e.getMessage());}
  @ExceptionHandler(LoadoutNotFoundException.class) @ResponseStatus(HttpStatus.NOT_FOUND) public Map<String,String> missing(LoadoutNotFoundException e){return Map.of("error",e.getMessage());}
}
