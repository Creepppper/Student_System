#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

load_local_env() {
  local env_file="$PROJECT_ROOT/.env.local"
  if [[ -f "$env_file" ]]; then
    if find "$env_file" -perm /077 -print -quit | grep -q .; then
      printf 'Private environment file is too permissive: %s\nRun: chmod 600 %q\n' \
        "$env_file" "$env_file" >&2
      exit 1
    fi
    # .env.local uses shell-compatible KEY='value' assignments and is ignored by Git.
    set -a
    # shellcheck disable=SC1090
    source "$env_file"
    set +a
  fi
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf 'Required command not found: %s\n' "$1" >&2
    exit 1
  fi
}
