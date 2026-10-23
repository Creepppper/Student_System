#!/usr/bin/env bash
set -euo pipefail
umask 077

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
db_name="student_system"
db_user="student_app"
admin_client=(sudo mariadb)
client_config=""
created_project_objects=false

cleanup() {
  local exit_status=$?
  trap - EXIT

  if [[ -n "$client_config" ]]; then
    rm -f -- "$client_config"
  fi

  if [[ "$exit_status" -ne 0 && "$created_project_objects" == true ]]; then
    printf 'Initialization failed; removing only the database and accounts created by this run.\n' >&2
    if ! "${admin_client[@]}" <<SQL
DROP USER IF EXISTS '${db_user}'@'localhost';
DROP USER IF EXISTS '${db_user}'@'127.0.0.1';
DROP DATABASE IF EXISTS ${db_name};
SQL
    then
      printf 'Automatic cleanup failed; inspect %s and %s manually.\n' \
        "$db_name" "$db_user" >&2
    fi
  fi

  exit "$exit_status"
}
trap cleanup EXIT

prompt_for_password() {
  local prompt_text=$1
  local confirm_new=${2:-false}
  local password_confirmation

  printf '%s for %s (input hidden): ' "$prompt_text" "$db_user"
  read -r -s app_password
  printf '\n'

  if (( ${#app_password} < 6 )); then
    printf 'Password must contain at least 6 characters.\n' >&2
    exit 1
  fi

  # Keeping credentials out of argv and restricting metacharacters makes the
  # SQL statement and temporary MariaDB option file safe without logging them.
  if [[ ! "$app_password" =~ ^[A-Za-z0-9_@%+=:,./!?-]+$ ]]; then
    printf 'Password may only contain letters, digits, and _@%%+=:,./!?-.\n' >&2
    exit 1
  fi

  if [[ "$confirm_new" == true ]]; then
    printf 'Confirm new password for %s (input hidden): ' "$db_user"
    read -r -s password_confirmation
    printf '\n'
    if [[ "$app_password" != "$password_confirmation" ]]; then
      printf 'Passwords do not match.\n' >&2
      exit 1
    fi
  fi
}

create_client_config() {
  client_config=$(mktemp)
  chmod 600 "$client_config"
  printf '[client]\nuser=%s\npassword=%s\nhost=127.0.0.1\nprotocol=TCP\n' \
    "$db_user" "$app_password" > "$client_config"
}

verify_database() {
  local table_count quartz_table_count critical_table_count user_count menu_count

  table_count=$(mariadb --defaults-extra-file="$client_config" "$db_name" \
    --batch --skip-column-names --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${db_name}'")
  quartz_table_count=$(mariadb --defaults-extra-file="$client_config" "$db_name" \
    --batch --skip-column-names --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${db_name}' AND LEFT(table_name, 5) = 'QRTZ_'")
  critical_table_count=$(mariadb --defaults-extra-file="$client_config" "$db_name" \
    --batch --skip-column-names --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${db_name}' AND table_name IN ('sys_user', 'sys_role', 'sys_menu', 'gen_table', 'QRTZ_JOB_DETAILS')")
  user_count=$(mariadb --defaults-extra-file="$client_config" "$db_name" \
    --batch --skip-column-names --execute='SELECT COUNT(*) FROM sys_user')
  menu_count=$(mariadb --defaults-extra-file="$client_config" "$db_name" \
    --batch --skip-column-names --execute='SELECT COUNT(*) FROM sys_menu')

  if [[ "$table_count" != "31" || "$quartz_table_count" != "11" || \
        "$critical_table_count" != "5" || "$user_count" -lt 1 || "$menu_count" -lt 1 ]]; then
    printf 'Database verification failed: tables=%s, Quartz=%s, critical=%s, users=%s, menus=%s.\n' \
      "$table_count" "$quartz_table_count" "$critical_table_count" "$user_count" "$menu_count" >&2
    return 1
  fi

  mariadb --defaults-extra-file="$client_config" "$db_name" --table <<'SQL'
SELECT CURRENT_USER() AS authenticated_account,
       DATABASE() AS current_database,
       @@character_set_database AS character_set,
       @@collation_database AS collation_name;
SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = DATABASE();
SELECT COUNT(*) AS initial_user_count FROM sys_user;
SELECT COUNT(*) AS menu_count FROM sys_menu;
SELECT COUNT(*) AS quartz_table_count
FROM information_schema.tables
WHERE table_schema = DATABASE() AND LEFT(table_name, 5) = 'QRTZ_';
SELECT privilege_type
FROM information_schema.schema_privileges
WHERE table_schema = DATABASE()
ORDER BY privilege_type;
SQL
}

printf 'This script uses Fedora local administrator authentication via sudo mariadb.\n'
printf 'It does not change the MariaDB root password or authentication plugin.\n'

database_exists=$("${admin_client[@]}" --batch --skip-column-names \
  --execute="SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = '${db_name}'")
account_count=$("${admin_client[@]}" --batch --skip-column-names \
  --execute="SELECT COUNT(*) FROM mysql.user WHERE User = '${db_user}' AND Host IN ('localhost', '127.0.0.1')")

if [[ "$database_exists" == "1" && "$account_count" == "2" ]]; then
  printf 'Existing project database and both local accounts found; running verification only.\n'
  printf 'Initialization SQL will not be executed because it contains DROP TABLE statements.\n'
  prompt_for_password 'Existing password'
  create_client_config
  verify_database
  printf 'Existing database %s passed application-account and schema verification.\n' "$db_name"
  exit 0
fi

if [[ "$database_exists" != "0" || "$account_count" != "0" ]]; then
  printf 'Partial project state detected (database=%s, local accounts=%s); no changes were made.\n' \
    "$database_exists" "$account_count" >&2
  printf 'Inspect the existing %s database and %s accounts before retrying.\n' \
    "$db_name" "$db_user" >&2
  exit 2
fi

printf 'The target database and accounts do not exist; a new isolated installation will be created.\n'
printf 'Upstream SQL contains DROP TABLE statements and is therefore run only in this new database.\n'
prompt_for_password 'New password' true
create_client_config

"${admin_client[@]}" --execute="CREATE DATABASE ${db_name} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
created_project_objects=true

"${admin_client[@]}" "$db_name" < "$project_root/sql/ry_20260417.sql"
"${admin_client[@]}" "$db_name" < "$project_root/sql/quartz.sql"

"${admin_client[@]}" <<SQL
CREATE USER '${db_user}'@'localhost' IDENTIFIED BY '${app_password}';
CREATE USER '${db_user}'@'127.0.0.1' IDENTIFIED BY '${app_password}';
GRANT SELECT, INSERT, UPDATE, DELETE
ON ${db_name}.* TO '${db_user}'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE
ON ${db_name}.* TO '${db_user}'@'127.0.0.1';
SQL

verify_database
created_project_objects=false
printf 'Database %s initialized and verified. Export DB_PASSWORD and TOKEN_SECRET before starting the backend.\n' "$db_name"
