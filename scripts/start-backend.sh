#!/usr/bin/env bash

set -euo pipefail
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$script_dir/common.sh"

require_command java
load_local_env

: "${DB_PASSWORD:?DB_PASSWORD is required. Export it or create an ignored .env.local file.}"
: "${TOKEN_SECRET:?TOKEN_SECRET is required. Export it or create an ignored .env.local file.}"
if [[ "$DB_PASSWORD" == 'replace-with-a-local-password' ||
      "$TOKEN_SECRET" == 'replace-with-at-least-32-random-characters' ]]; then
  printf 'Replace the placeholder credentials before starting the backend.\n' >&2
  exit 1
fi
if (( ${#TOKEN_SECRET} < 32 )); then
  printf 'TOKEN_SECRET must contain at least 32 characters.\n' >&2
  exit 1
fi

export DB_USERNAME="${DB_USERNAME:-student_app}"
export DB_URL="${DB_URL:-jdbc:mysql://127.0.0.1:3306/student_system?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia%2FShanghai&allowPublicKeyRetrieval=true}"
export VALKEY_HOST="${VALKEY_HOST:-127.0.0.1}"
export VALKEY_PORT="${VALKEY_PORT:-6379}"
export VALKEY_DATABASE="${VALKEY_DATABASE:-0}"
export VALKEY_PASSWORD="${VALKEY_PASSWORD:-}"
export SERVER_PORT="${SERVER_PORT:-8080}"
export LOG_PATH="${LOG_PATH:-$PROJECT_ROOT/logs}"
export RUOYI_PROFILE="${RUOYI_PROFILE:-$PROJECT_ROOT/data/uploadPath}"

# Resolve portable relative paths against the project root, not the caller's cwd.
if [[ "$LOG_PATH" != /* ]]; then
  export LOG_PATH="$PROJECT_ROOT/$LOG_PATH"
fi
if [[ "$RUOYI_PROFILE" != /* ]]; then
  export RUOYI_PROFILE="$PROJECT_ROOT/$RUOYI_PROFILE"
fi

jar_path="$PROJECT_ROOT/ruoyi-admin/target/ruoyi-admin.jar"
if [[ ! -f "$jar_path" ]]; then
  printf 'Backend JAR not found: %s\nRun scripts/build-backend.sh first.\n' "$jar_path" >&2
  exit 1
fi

if command -v ss >/dev/null 2>&1 && [[ -n "$(ss -ltnH "sport = :$SERVER_PORT" 2>/dev/null || true)" ]]; then
  printf 'Port %s is already in use; refusing to start a duplicate backend.\n' "$SERVER_PORT" >&2
  exit 1
fi

mkdir -p "$LOG_PATH" "$RUOYI_PROFILE"
cd "$PROJECT_ROOT"
printf 'Starting backend on http://localhost:%s\n' "$SERVER_PORT"
printf 'Log directory: %s\n' "$LOG_PATH"
exec java -Duser.timezone=Asia/Shanghai -jar "$jar_path"
