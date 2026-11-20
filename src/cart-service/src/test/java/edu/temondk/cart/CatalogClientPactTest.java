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
  @Pact(provider="catalog-service",consumer="cart-service")
  public RequestResponsePact inactiveProduct(PactDslWithProvider builder){
    return builder.given("product SKU-OFF is inactive")
      .uponReceiving("Cart checks inactive product SKU-OFF").path("/products/SKU-OFF").method("GET")
      .willRespondWith().status(422).headers(java.util.Map.of("Content-Type","application/problem+json"))
      .body("{\"type\":\"about:blank\",\"title\":\"Unprocessable Entity\",\"status\":422,\"detail\":\"Товар снят с продажи\",\"instance\":\"/products/SKU-OFF\"}")
      .toPact();
  }
  @Test @PactTestFor(providerName="catalog-service",pactMethod="product",pactVersion=PactSpecVersion.V3)
  void clientReadsProviderContract(MockServer mock){
    CatalogClient.Product p=new CatalogClient(mock.getUrl()).get("SKU-123");
    assertEquals("SKU-123",p.sku());assertEquals(0,new java.math.BigDecimal("125.50").compareTo(p.price()));
    assertTrue(p.active());
  }
  @Test @PactTestFor(providerName="catalog-service",pactMethod="inactiveProduct",pactVersion=PactSpecVersion.V3)
  void clientRejectsInactiveProduct(MockServer mock){
    assertThrows(CatalogClient.CatalogInactive.class,()->new CatalogClient(mock.getUrl()).get("SKU-OFF"));
  }
}
