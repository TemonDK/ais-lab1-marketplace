package edu.temondk.catalog;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
@Validated @RestController @RequestMapping("/products") public class ProductController {
  private final JdbcTemplate db;
  public ProductController(JdbcTemplate db) {this.db=db;}
  @PostMapping @ResponseStatus(HttpStatus.CREATED)
  public Product create(@Valid @RequestBody Product input) {
    try {db.update("insert into products(sku,title,price,active) values (?,?,?,?)",input.sku(),input.title(),input.price(),input.active());}
    catch (DuplicateKeyException ex) {throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"SKU уже существует");}
    return input;
  }
  @GetMapping("/{sku}") public Product get(@PathVariable @Pattern(regexp="[A-Z0-9-]{3,32}") String sku) {
    Product product = db.query("select sku,title,price,active from products where sku=?",(rs,n)->new Product(rs.getString(1),rs.getString(2),rs.getBigDecimal(3),rs.getBoolean(4)),sku)
      .stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Товар не найден"));
    if (!product.active()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Товар снят с продажи");
    return product;
  }
  @PutMapping("/{sku}") public Product update(@PathVariable @Pattern(regexp="[A-Z0-9-]{3,32}") String sku,@Valid @RequestBody Product input) {
    if (!sku.equals(input.sku())) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"SKU в пути и теле различаются");
    int changed=db.update("update products set title=?,price=?,active=? where sku=?",input.title(),input.price(),input.active(),sku);
    if(changed==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Товар не найден");
    return input;
  }
}
