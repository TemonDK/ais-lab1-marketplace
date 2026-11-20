package edu.temondk.catalog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice public class Errors {
  private ProblemDetail problem(HttpStatus status,String detail,HttpServletRequest request) {
    ProblemDetail p=ProblemDetail.forStatusAndDetail(status,detail);
    p.setTitle(status.getReasonPhrase());p.setInstance(java.net.URI.create(request.getRequestURI()));return p;
  }
  @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class,ConstraintViolationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST) public ProblemDetail invalid(Exception ex,HttpServletRequest req) {
    return problem(HttpStatus.BAD_REQUEST,"Некорректный запрос к каталогу",req);
  }
  @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<ProblemDetail> domain(ResponseStatusException ex,HttpServletRequest req) {
    HttpStatus status=HttpStatus.valueOf(ex.getStatusCode().value());
    return ResponseEntity.status(status).contentType(MediaType.parseMediaType("application/problem+json"))
      .body(problem(status,ex.getReason(),req));
  }
  @ExceptionHandler(Exception.class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ProblemDetail server(Exception ex,HttpServletRequest req) {return problem(HttpStatus.INTERNAL_SERVER_ERROR,"Внутренняя ошибка каталога",req);}
}
