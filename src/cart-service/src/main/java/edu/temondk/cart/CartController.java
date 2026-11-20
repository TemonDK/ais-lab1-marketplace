package edu.temondk.cart;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@Validated @RestController @RequestMapping("/carts/{buyerId}") public class CartController {
  public record AddItem(@NotBlank @Pattern(regexp="[A-Z0-9-]{3,32}") String sku,@Min(1) @Max(100) int quantity) {}
  public record Item(String sku,String title,BigDecimal unitPrice,int quantity) {}
  public record Cart(String buyerId,List<Item> items,BigDecimal total) {}
  private final JdbcTemplate db;private final CatalogClient catalog;
  public CartController(JdbcTemplate db,CatalogClient catalog){this.db=db;this.catalog=catalog;}
  @PostMapping("/items") @ResponseStatus(HttpStatus.CREATED)
  public Cart add(@PathVariable @Pattern(regexp="[a-zA-Z0-9-]{3,40}") String buyerId,@Valid @RequestBody AddItem input){
    CatalogClient.Product p=catalog.get(input.sku());
    if(Boolean.FALSE.equals(p.active())) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Товар снят с продажи");
    db.update("insert into cart_items(buyer_id,sku,title,unit_price,quantity) values (?,?,?,?,?) on conflict(buyer_id,sku) do update set title=excluded.title,unit_price=excluded.unit_price,quantity=cart_items.quantity+excluded.quantity",
      buyerId,p.sku(),p.title(),p.price(),input.quantity());
    return read(buyerId);
  }
  @GetMapping public Cart read(@PathVariable @Pattern(regexp="[a-zA-Z0-9-]{3,40}") String buyerId){
    List<Item> items=db.query("select sku,title,unit_price,quantity from cart_items where buyer_id=? order by sku",(rs,n)->new Item(rs.getString(1),rs.getString(2),rs.getBigDecimal(3),rs.getInt(4)),buyerId);
    BigDecimal total=items.stream().map(i->i.unitPrice().multiply(BigDecimal.valueOf(i.quantity()))).reduce(BigDecimal.ZERO,BigDecimal::add);
    return new Cart(buyerId,items,total);
  }
  @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clear(@PathVariable @Pattern(regexp="[a-zA-Z0-9-]{3,40}") String buyerId){db.update("delete from cart_items where buyer_id=?",buyerId);}
}
