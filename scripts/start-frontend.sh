#!/usr/bin/env bash

set -euo pipefail
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$script_dir/common.sh"

require_command node
require_command npm

frontend_dir="$PROJECT_ROOT/frontend"
if [[ ! -d "$frontend_dir/node_modules" ]]; then
  printf 'Frontend dependencies are missing. Run this once first:\n  cd %s && npm install\n' "$frontend_dir" >&2
  exit 1
fi

cd "$frontend_dir"
exec npm run dev -- --strictPort "$@"
