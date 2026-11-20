package edu.temondk.catalog;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.spring.spring6.PactVerificationSpring6Provider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("catalog-service") @PactFolder("../../tests/pact/pacts")
public class CatalogPactVerificationTest {
  @LocalServerPort int port;
  @Autowired JdbcTemplate db;
  @BeforeEach void target(PactVerificationContext context){context.setTarget(new HttpTestTarget("localhost",port));}
  @State("product SKU-123 exists") void seed(){
    db.update("insert into products(sku,title,price,active) values (?,?,?,?) on conflict(sku) do update set title=excluded.title,price=excluded.price,active=excluded.active",
      "SKU-123","Учебный товар",new java.math.BigDecimal("125.50"),true);
  }
  @State("product SKU-OFF is inactive") void seedInactive(){
    db.update("insert into products(sku,title,price,active) values (?,?,?,?) on conflict(sku) do update set title=excluded.title,price=excluded.price,active=excluded.active",
      "SKU-OFF","Неактивный товар",new java.math.BigDecimal("25.00"),false);
  }
  @TestTemplate @ExtendWith(PactVerificationSpring6Provider.class)
  void verify(PactVerificationContext context){context.verifyInteraction();}
}
