package edu.temondk.cart;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice public class Errors {
  private static final Logger log=LoggerFactory.getLogger(Errors.class);
  private ProblemDetail p(HttpStatus s,String detail,HttpServletRequest req){
    ProblemDetail p=ProblemDetail.forStatusAndDetail(s,detail);p.setTitle(s.getReasonPhrase());p.setInstance(java.net.URI.create(req.getRequestURI()));return p;
  }
  @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class,ConstraintViolationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST) public ProblemDetail invalid(Exception ex,HttpServletRequest req){return p(HttpStatus.BAD_REQUEST,"Некорректный запрос к корзине",req);}
  @ExceptionHandler(CatalogClient.CatalogMissing.class) @ResponseStatus(HttpStatus.NOT_FOUND)
  public ProblemDetail missing(Exception ex,HttpServletRequest req){return p(HttpStatus.NOT_FOUND,"Товар не найден в каталоге",req);}
  @ExceptionHandler(CatalogClient.CatalogUnavailable.class) @ResponseStatus(HttpStatus.BAD_GATEWAY)
  public ProblemDetail unavailable(Exception ex,HttpServletRequest req){return p(HttpStatus.BAD_GATEWAY,"Каталог недоступен",req);}
  @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<ProblemDetail> domain(ResponseStatusException ex,HttpServletRequest req){HttpStatus s=HttpStatus.valueOf(ex.getStatusCode().value());return ResponseEntity.status(s).contentType(MediaType.parseMediaType("application/problem+json")).body(p(s,ex.getReason(),req));}
  @ExceptionHandler(Exception.class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ProblemDetail server(Exception ex,HttpServletRequest req){log.error("Unexpected error",ex);return p(HttpStatus.INTERNAL_SERVER_ERROR,"Внутренняя ошибка корзины",req);}
}
