#!/usr/bin/env bash
set -euo pipefail
base="${GATEWAY_URL:-http://localhost:8080}"
count=0
for n in $(seq 1 30); do
  code="$(curl -sS -o /dev/null -w '%{http_code}' "$base/api/catalog/products/SKU-NONE")"
  printf 'Запрос %02d: HTTP %s\n' "$n" "$code"
  if [ "$code" = 429 ]; then count=$((count+1)); fi
done
printf 'Отфильтровано (429): %d\n' "$count"
test "$count" -gt 0
