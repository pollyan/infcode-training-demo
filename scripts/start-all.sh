#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"

mkdir -p "$RUN_DIR"

require_command() {
  local name="$1"
  if ! command -v "$name" >/dev/null 2>&1; then
    printf 'Missing required command: %s\n' "$name" >&2
    exit 1
  fi
}

port_in_use() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
    return $?
  fi
  return 1
}

start_service() {
  local name="$1"
  local workdir="$2"
  local command="$3"
  local port="$4"
  local pid_file="$RUN_DIR/$name.pid"
  local log_file="$RUN_DIR/$name.log"

  if [ -f "$pid_file" ] && kill -0 "$(cat "$pid_file")" >/dev/null 2>&1; then
    printf '[SKIP] %s is already running with pid %s\n' "$name" "$(cat "$pid_file")"
    return 0
  fi

  if port_in_use "$port"; then
    printf '[ERROR] Port %s is already in use, cannot start %s.\n' "$port" "$name" >&2
    exit 1
  fi

  printf '[START] %s\n' "$name"
  (
    cd "$workdir"
    nohup bash -lc "$command" >"$log_file" 2>&1 &
    echo $! >"$pid_file"
  )
}

print_summary() {
  printf '\nServices started. Logs are in %s\n' "$RUN_DIR"
  printf 'Frontend:    http://127.0.0.1:5173\n'
  printf 'Backend:     http://127.0.0.1:8080/api/health\n'
  printf 'Mock server: http://127.0.0.1:9090/health\n'
  printf '\nUse scripts/stop-all.sh to stop all services.\n'
}

require_command node
require_command npm
require_command java
require_command mvn

"$ROOT_DIR/scripts/check-env.sh"
"$ROOT_DIR/scripts/install-deps.sh"

start_service "mock-server" "$ROOT_DIR/mock-server" "npm start" "9090"
start_service "backend" "$ROOT_DIR/backend" "mvn spring-boot:run" "8080"
start_service "frontend" "$ROOT_DIR/frontend" "npm start" "5173"

print_summary
