#!/usr/bin/env bash

set -euo pipefail
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

case "${1:-start}" in
  start)
    exec "$project_root/scripts/start-backend.sh"
    ;;
  status)
    if command -v ss >/dev/null 2>&1 && [[ -n "$(ss -ltnH 'sport = :8080' 2>/dev/null || true)" ]]; then
      printf 'A service is listening on port 8080.\n'
    else
      printf 'No service is listening on port 8080.\n'
    fi
    ;;
  stop|restart)
    printf 'Automatic %s is disabled to avoid terminating an unrelated process.\n' "$1" >&2
    printf 'Stop the foreground backend with Ctrl+C, then start it again explicitly.\n' >&2
    exit 2
    ;;
  *)
    printf 'Usage: %s {start|status}\n' "$0" >&2
    exit 2
    ;;
esac
