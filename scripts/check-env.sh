#!/usr/bin/env bash

set -euo pipefail
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$script_dir/common.sh"

load_local_env
printf 'Project root: %s\n' "$PROJECT_ROOT"

for tool in java mvn node npm mariadb valkey-cli; do
  if command -v "$tool" >/dev/null 2>&1; then
    printf '[OK] %s: %s\n' "$tool" "$(command -v "$tool")"
  else
    printf '[MISSING] %s\n' "$tool"
  fi
done

printf '\nVersions:\n'
java -version 2>&1 | head -1 || true
mvn -version 2>/dev/null | head -2 || true
node --version 2>/dev/null || true
npm --version 2>/dev/null || true
mariadb --version 2>/dev/null || true
valkey-cli --version 2>/dev/null || true

printf '\nMaven repository:\n%s\n' "${HOME:?}/.m2/repository"
if [[ -d "$PROJECT_ROOT/.m2/repository" ]]; then
  printf '[NOTICE] Legacy project Maven repository still exists: %s/.m2/repository\n' "$PROJECT_ROOT"
fi

if valkey-cli -h "${VALKEY_HOST:-127.0.0.1}" -p "${VALKEY_PORT:-6379}" ping 2>/dev/null | grep -qx PONG; then
  printf '[OK] Valkey PING/PONG\n'
else
  printf '[WARN] Valkey connection was not verified.\n'
fi

if [[ -n "${DB_PASSWORD:-}" ]]; then
  db_config=$(mktemp)
  trap 'rm -f "$db_config"' EXIT
  chmod 600 "$db_config"
  printf '[client]\nuser=%s\npassword=%s\nhost=127.0.0.1\nprotocol=TCP\n' \
    "${DB_USERNAME:-student_app}" "$DB_PASSWORD" > "$db_config"
  if mariadb --defaults-extra-file="$db_config" student_system \
      --batch --skip-column-names --execute='SELECT 1' 2>/dev/null | grep -qx 1; then
    printf '[OK] MariaDB application-account connection\n'
  else
    printf '[WARN] MariaDB application-account connection failed.\n'
  fi
else
  printf '[SKIP] DB_PASSWORD is not set; MariaDB application-account check skipped.\n'
fi
