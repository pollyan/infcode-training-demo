#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

printf 'Installing Node.js dependencies for frontend...\n'

for dir in frontend; do
  printf '\n[%s] npm install\n' "$dir"
  (
    cd "$ROOT_DIR/$dir"
    npm install
  )
done

printf '\nDependency installation completed.\n'
