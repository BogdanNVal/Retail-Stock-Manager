#!/bin/sh
# Bind $PORT before the JVM so Render doesn't restart with
# "New primary port detected".
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
WAR="${WAR_PATH:-$SCRIPT_DIR/app.war}"
PROXY_PY="$SCRIPT_DIR/early_proxy.py"

hosted=0
if [ -n "${RENDER:-}" ] || [ -n "${K_SERVICE:-}" ]; then
  hosted=1
elif [ -n "${PORT:-}" ] && printf '%s' "${SPRING_PROFILES_ACTIVE:-}" | grep -Eq '(^|,)[[:space:]]*prod[[:space:]]*(,|$)'; then
  hosted=1
fi

if [ "$hosted" -eq 1 ]; then
  PUBLIC_PORT="${PORT:-8080}"
  if [ "$PUBLIC_PORT" = "8080" ]; then
    INTERNAL_PORT=8081
  else
    INTERNAL_PORT=8080
  fi
  export RETAIL_ENTRYPOINT_PROXY=1
  export SERVER_PORT="$INTERNAL_PORT"
  export SERVER_ADDRESS=127.0.0.1
  python3 "$PROXY_PY" "$PUBLIC_PORT" "$INTERNAL_PORT" &
  PROXY_PID=$!
  # Wait until the port is actually open so Render's first scan succeeds.
  i=0
  while [ "$i" -lt 50 ]; do
    if python3 -c "import socket;s=socket.socket();s.settimeout(0.2);s.connect(('127.0.0.1', int('$PUBLIC_PORT')));s.close()" 2>/dev/null; then
      break
    fi
    if ! kill -0 "$PROXY_PID" 2>/dev/null; then
      echo "early_proxy.py exited before binding port $PUBLIC_PORT" >&2
      exit 1
    fi
    i=$((i + 1))
    sleep 0.1
  done
  if ! python3 -c "import socket;s=socket.socket();s.settimeout(0.2);s.connect(('127.0.0.1', int('$PUBLIC_PORT')));s.close()" 2>/dev/null; then
    echo "early_proxy.py failed to bind 0.0.0.0:$PUBLIC_PORT" >&2
    kill "$PROXY_PID" 2>/dev/null || true
    exit 1
  fi
fi

exec java -jar "$WAR"
