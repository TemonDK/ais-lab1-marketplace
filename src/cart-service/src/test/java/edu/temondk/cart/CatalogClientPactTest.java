package edu.temondk.cart;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.PactSpecVersion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@PactConsumerTest public class CatalogClientPactTest {
  @Pact(provider="catalog-service",consumer="cart-service")
  public RequestResponsePact product(PactDslWithProvider builder){
    return builder.given("product SKU-123 exists")
      .uponReceiving("Cart looks up product SKU-123").path("/products/SKU-123").method("GET")
      .willRespondWith().status(200).headers(java.util.Map.of("Content-Type","application/json"))
      .body("{\"sku\":\"SKU-123\",\"title\":\"Учебный товар\",\"price\":125.50,\"active\":true}")
      .toPact();
  }
  @Test @PactTestFor(providerName="catalog-service",pactMethod="product",pactVersion=PactSpecVersion.V3)
  void clientReadsProviderContract(MockServer mock){
    CatalogClient.Product p=new CatalogClient(mock.getUrl()).get("SKU-123");
    assertEquals("SKU-123",p.sku());assertEquals(0,new java.math.BigDecimal("125.50").compareTo(p.price()));
    assertTrue(p.active());
  }
}
