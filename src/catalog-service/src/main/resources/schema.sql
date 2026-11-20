CREATE TABLE IF NOT EXISTS products (
  sku varchar(32) PRIMARY KEY,
  title varchar(120) NOT NULL,
  price numeric(12,2) NOT NULL CHECK (price > 0),
  active boolean NOT NULL
);
