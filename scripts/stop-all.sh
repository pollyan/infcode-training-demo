#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"

stop_service() {
  local name="$1"
  local pid_file="$RUN_DIR/$name.pid"

  if [ ! -f "$pid_file" ]; then
    printf '[SKIP] %s pid file not found.\n' "$name"
    return 0
  fi

  local pid
  pid="$(cat "$pid_file")"

  if kill -0 "$pid" >/dev/null 2>&1; then
    printf '[STOP] %s (pid %s)\n' "$name" "$pid"
    kill "$pid"
  else
    printf '[CLEAN] %s pid file existed but process is already gone.\n' "$name"
  fi

  rm -f "$pid_file"
}

stop_service "frontend"
stop_service "backend"
stop_service "mock-server"

printf '\nAll known services have been stopped.\n'
