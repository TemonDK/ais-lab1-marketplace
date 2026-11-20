#!/usr/bin/env bash
set -euo pipefail
base="${GATEWAY_URL:-http://localhost:8080}"
files="$(mktemp -d)"; trap 'rm -rf "$files"' EXIT
sku="SKU-$(date +%s)"
buyer="buyer-$(date +%s)"
check() {
  local expected="$1" label="$2"; shift 2
  local code
  code="$(curl -sS -o "$files/body" -D "$files/headers" -w '%{http_code}' "$@")"
  printf '%s: HTTP %s (ожидался %s)\n' "$label" "$code" "$expected"
  if [ "$code" != "$expected" ]; then cat "$files/body"; exit 1; fi
  if [[ "$code" =~ ^(400|404|422)$ ]]; then
    grep -qi 'content-type: application/problem+json' "$files/headers"
    python3 - "$files/body" "$code" <<'PY'
import json,sys
p=json.load(open(sys.argv[1]));assert p['status']==int(sys.argv[2]);assert all(p.get(k) for k in ('type','title','detail','instance'))
PY
  fi
  sleep 0.3
}
product="{\"sku\":\"$sku\",\"title\":\"Товар для проверки\",\"price\":125.50,\"active\":true}"
check 201 'Создание товара' -H 'Content-Type: application/json' -d "$product" "$base/api/catalog/products"
check 422 'Повтор SKU' -H 'Content-Type: application/json' -d "$product" "$base/api/catalog/products"
check 200 'Чтение товара' "$base/api/catalog/products/$sku"
check 200 'Обновление товара' -X PUT -H 'Content-Type: application/json' -d "$product" "$base/api/catalog/products/$sku"
check 404 'Неизвестный товар' "$base/api/catalog/products/SKU-UNKNOWN"
check 400 'Невалидный товар' -H 'Content-Type: application/json' -d '{"sku":"BAD","title":"x","price":-1,"active":true}' "$base/api/catalog/products"
check 201 'Корзина вызывает каталог' -H 'Content-Type: application/json' -d "{\"sku\":\"$sku\",\"quantity\":2}" "$base/api/cart/$buyer/items"
check 200 'Чтение корзины' "$base/api/cart/$buyer"
python3 - "$files/body" "$sku" <<'PY'
import json,sys
c=json.load(open(sys.argv[1]));assert c['items'][0]['sku']==sys.argv[2];assert c['items'][0]['quantity']==2;assert float(c['total'])==251
PY
check 400 'Неверное количество' -H 'Content-Type: application/json' -d "{\"sku\":\"$sku\",\"quantity\":0}" "$base/api/cart/$buyer/items"
check 404 'Отсутствующий товар в корзине' -H 'Content-Type: application/json' -d '{"sku":"SKU-MISSING","quantity":1}' "$base/api/cart/$buyer/items"
check 204 'Очистка корзины' -X DELETE "$base/api/cart/$buyer"
check 200 'Корзина после очистки' "$base/api/cart/$buyer"
python3 - "$files/body" <<'PY'
import json,sys
assert json.load(open(sys.argv[1]))['items']==[]
PY
echo 'Все проверки API пройдены.'
