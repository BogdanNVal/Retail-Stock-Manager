#!/usr/bin/env bash
# Load sample products via the REST API (POST requires authentication).
#
# Usage (from project root):
#   ./test-data/incarca-produse-test.sh
#
# Optional env vars:
#   BASE_URL=http://localhost:8080
#   APP_ADMIN_USERNAME=admin
#   APP_ADMIN_PASSWORD=admin123

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USER="${APP_ADMIN_USERNAME:-admin}"
PASS="${APP_ADMIN_PASSWORD:-admin123}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JSON_FILE="$SCRIPT_DIR/produse-test.json"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required (sudo apt install jq / brew install jq)" >&2
  exit 1
fi

count="$(jq 'length' "$JSON_FILE")"
echo "Se incarca $count produse in $BASE_URL/api/produse ..."

failures=0
while read -r produs; do
  nume="$(echo "$produs" | jq -r '.nume')"
  if curl -sS -f -u "$USER:$PASS" -H "Content-Type: application/json" \
      -d "$produs" "$BASE_URL/api/produse" >/tmp/retail-seed-response.json; then
    id="$(jq -r '.id // empty' /tmp/retail-seed-response.json 2>/dev/null || true)"
    echo "OK   -> $nume${id:+ (id: $id)}"
  else
    echo "EROARE -> $nume (HTTP failure; vezi autentificare sau EAN duplicat)"
    failures=$((failures + 1))
  fi
done < <(jq -c '.[]' "$JSON_FILE")

if [[ "$failures" -gt 0 ]]; then
  echo "Finalizat cu $failures erori." >&2
  exit 1
fi

echo "Gata. Verifica la $BASE_URL/api/produse sau $BASE_URL/produse"
