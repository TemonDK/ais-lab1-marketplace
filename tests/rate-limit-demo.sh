#!/usr/bin/env bash
set -euo pipefail
base="${GATEWAY_URL:-http://localhost:8080}"
count=0
files="$(mktemp -d)"; trap 'rm -rf "$files"' EXIT
for n in $(seq 1 30); do
  code="$(curl -sS -o "$files/body" -D "$files/headers" -w '%{http_code}' "$base/api/catalog/products/SKU-NONE")"
  printf 'Запрос %02d: HTTP %s\n' "$n" "$code"
  if [ "$code" = 429 ]; then
    if [ "$count" = 0 ]; then
      grep -qi 'content-type: application/problem+json' "$files/headers"
      python3 - "$files/body" <<'PY'
import json,sys
p=json.load(open(sys.argv[1]));assert p['status']==429;assert all(p.get(k) for k in ('type','title','detail','instance'))
PY
    fi
    count=$((count+1))
  fi
done
printf 'Отфильтровано (429): %d\n' "$count"
echo 'Первый ответ 429 соответствует формату ProblemDetails.'
test "$count" -gt 0
