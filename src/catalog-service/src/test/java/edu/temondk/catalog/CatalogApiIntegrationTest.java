package edu.temondk.catalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate db;

  @BeforeEach void clean() {db.update("delete from products where sku=?", "SKU-TEST");}

  @Test void productLifecycleAndProblemDetails() throws Exception {
    String product = "{\"sku\":\"SKU-TEST\",\"title\":\"Товар для теста\",\"price\":125.50,\"active\":true}";
    mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(product))
      .andExpect(status().isCreated()).andExpect(jsonPath("$.sku").value("SKU-TEST"));
    mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(product))
      .andExpect(status().isUnprocessableEntity()).andExpect(content().contentTypeCompatibleWith("application/problem+json"))
      .andExpect(jsonPath("$.status").value(422));
    mvc.perform(get("/products/SKU-TEST")).andExpect(status().isOk()).andExpect(jsonPath("$.price").value(125.5));
    mvc.perform(put("/products/SKU-TEST").contentType(MediaType.APPLICATION_JSON)
      .content(product.replace("\"active\":true", "\"active\":false")))
      .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
    mvc.perform(get("/products/SKU-TEST")).andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.status").value(422));
  }

  @Test void invalidInputAndMissingProduct() throws Exception {
    mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
      .content("{\"sku\":\"SKU-TEST\",\"title\":\"Товар\",\"price\":15.00}"))
      .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith("application/problem+json"))
      .andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.detail").isNotEmpty());
    mvc.perform(get("/products/SKU-ABSENT")).andExpect(status().isNotFound())
      .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
      .andExpect(jsonPath("$.status").value(404));
  }
}
