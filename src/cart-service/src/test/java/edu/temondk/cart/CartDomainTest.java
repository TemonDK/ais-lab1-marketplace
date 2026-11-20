package edu.temondk.cart;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartDomainTest {
  @Test void inactiveProductCannotBeStoredInCart() {
    JdbcTemplate db = mock(JdbcTemplate.class);
    CatalogClient catalog = mock(CatalogClient.class);
    when(catalog.get("SKU-OFF"))
      .thenReturn(new CatalogClient.Product("SKU-OFF", "Неактивный товар", new BigDecimal("25.00"), false));

    CartController cart = new CartController(db, catalog);
    ResponseStatusException failure = assertThrows(ResponseStatusException.class,
      () -> cart.add("buyer-1", new CartController.AddItem("SKU-OFF", 1)));
    assertEquals(422, failure.getStatusCode().value());
    verifyNoInteractions(db);
  }

  @Test void missingProductCannotBeStoredInCart() {
    JdbcTemplate db = mock(JdbcTemplate.class);
    CatalogClient catalog = mock(CatalogClient.class);
    when(catalog.get("SKU-NONE")).thenThrow(new CatalogClient.CatalogMissing());

    CartController cart = new CartController(db, catalog);
    assertThrows(CatalogClient.CatalogMissing.class,
      () -> cart.add("buyer-1", new CartController.AddItem("SKU-NONE", 1)));
    verifyNoInteractions(db);
  }
}
