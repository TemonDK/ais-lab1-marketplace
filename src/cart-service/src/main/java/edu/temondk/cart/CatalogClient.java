package edu.temondk.cart;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
@Component public class CatalogClient {
  private final RestClient client;
  public record Product(String sku,String title,BigDecimal price,Boolean active) {}
  public CatalogClient(@Value("${catalog.url:http://localhost:8081}") String url) {client=RestClient.builder().baseUrl(url).build();}
  public Product get(String sku) {
    try {return client.get().uri("/products/{sku}",sku).retrieve().body(Product.class);}
    catch(HttpClientErrorException.NotFound ex){throw new CatalogMissing();}
    catch(ResourceAccessException | HttpServerErrorException ex){throw new CatalogUnavailable();}
  }
  static class CatalogMissing extends RuntimeException {}
  static class CatalogUnavailable extends RuntimeException {}
}
