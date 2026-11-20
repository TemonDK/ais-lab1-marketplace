CREATE TABLE IF NOT EXISTS cart_items (
  buyer_id varchar(40) NOT NULL,
  sku varchar(32) NOT NULL,
  title varchar(120) NOT NULL,
  unit_price numeric(12,2) NOT NULL,
  quantity integer NOT NULL CHECK (quantity > 0),
  PRIMARY KEY (buyer_id,sku)
);
