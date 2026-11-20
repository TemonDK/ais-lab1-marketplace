package edu.temondk.catalog;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record Product(
  @NotBlank @Pattern(regexp="[A-Z0-9-]{3,32}") String sku,
  @NotBlank @Size(min=3,max=120) String title,
  @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal price,
  @NotNull Boolean active) {}
