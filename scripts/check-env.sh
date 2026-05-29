#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

print_header() {
  printf '\n== %s ==\n' "$1"
}

check_command() {
  local name="$1"
  if command -v "$name" >/dev/null 2>&1; then
    printf '[OK] %s: %s\n' "$name" "$(command -v "$name")"
    return 0
  fi

  printf '[MISSING] %s\n' "$name"
  return 1
}

print_header "InfCode Training Demo Environment Check"
printf 'Project: %s\n' "$ROOT_DIR"

missing=0

print_header "Required Commands"
for cmd in node npm java mvn git; do
  if ! check_command "$cmd"; then
    missing=1
  fi
done

print_header "Version Summary"
if command -v node >/dev/null 2>&1; then
  printf 'node: %s\n' "$(node -v)"
fi
if command -v npm >/dev/null 2>&1; then
  printf 'npm:  %s\n' "$(npm -v)"
fi
if command -v java >/dev/null 2>&1; then
  printf 'java: %s\n' "$(java -version 2>&1 | head -n 1)"
fi
if command -v mvn >/dev/null 2>&1; then
  printf 'mvn:  %s\n' "$(mvn -v 2>/dev/null | head -n 1)"
fi
if command -v git >/dev/null 2>&1; then
  printf 'git:  %s\n' "$(git --version 2>/dev/null)"
fi

print_header "Project Directories"
for dir in frontend backend mock-server docs .infcode/rules; do
  if [ -d "$ROOT_DIR/$dir" ]; then
    printf '[OK] %s\n' "$dir"
  else
    printf '[MISSING] %s\n' "$dir"
    missing=1
  fi
done

print_header "Git Project Check"
if [ -d "$ROOT_DIR/.git" ]; then
  printf '[OK] .git directory detected\n'
else
  printf '[MISSING] .git directory\n'
  missing=1
fi

if git -C "$ROOT_DIR" rev-parse --show-toplevel >/dev/null 2>&1; then
  printf '[OK] Project root is recognized by git\n'
else
  printf '[MISSING] Project root is not recognized by git\n'
  missing=1
fi

print_header "Training Notes"
echo "- Training should prefer the customer's real network, machine, IDE, and project when feasible."
echo "- For Java multi-module projects, open the full project from the repository root when possible."
echo "- Rules, DOCX documents, and knowledge-base materials should be prepared before class."

if [ "$missing" -ne 0 ]; then
  print_header "Result"
  printf 'Environment check failed. Please install missing tools or restore missing directories.\n'
  exit 1
fi

print_header "Result"
printf 'Environment check passed.\n'
