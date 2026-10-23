#!/usr/bin/env bash

set -euo pipefail
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$script_dir/common.sh"

require_command mvn
cd "$PROJECT_ROOT"

printf 'Building backend with the default Maven repository: %s/.m2/repository\n' "${HOME:?}"
exec mvn clean package "$@"
