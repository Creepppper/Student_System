#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
CHROME="${CHROME:-google-chrome}"
PROFILE="${PROFILE:-/tmp/dsh-prototype-chrome}"
mkdir -p "$ROOT/screenshots" "$PROFILE"
shot() {
  local file="$1" query="$2"
  "$CHROME" --headless=new --no-sandbox --disable-gpu --hide-scrollbars \
    --force-device-scale-factor=2 --window-size=1600,900 \
    --user-data-dir="$PROFILE" --run-all-compositor-stages-before-draw \
    --virtual-time-budget=900 \
    --screenshot="$ROOT/screenshots/$file.png" \
    "file://$ROOT/index.html?$query" >/dev/null 2>&1
  printf 'captured %s.png\n' "$file"
}

shot "UI-01-登录页面" "view=login&state=error"
shot "UI-01A-登录页面（正常）" "view=login"
shot "UI-02-学生信息管理" "view=students"
shot "UI-02A-学生信息编辑" "view=students&modal=edit"
shot "UI-03-班主任与班级管理" "view=classes"
shot "UI-03A-班主任变更" "view=classes&modal=assign"
shot "UI-04-考试管理" "view=exams"
shot "UI-04A-新建考试" "view=exams&modal=create"
shot "UI-05-成绩录入" "view=grades"
shot "UI-05A-成绩校验错误" "view=grades&state=invalid"
shot "UI-05B-退回后重提" "view=grades&state=returned"
shot "UI-06-成绩审核发布" "view=review"
shot "UI-06A-审核退回" "view=review&modal=return"
shot "UI-06B-发布确认" "view=review&modal=publish"
shot "UI-06C-撤回与更正" "view=review&modal=withdraw"
shot "UI-07-学生成绩查询" "view=query"
shot "UI-07A-成绩未发布" "view=query&state=unpublished"
shot "UI-08-本班成绩统计" "view=stats"
shot "UI-08C-全校成绩统计" "view=stats&role=academic"
shot "UI-08A-业务审计" "view=audit"
shot "UI-08B-系统审计脱敏" "view=audit&role=sys"
shot "UI-09-P1成绩复核申请" "view=review-apply"
shot "UI-10-P1复核处理（班主任）" "view=review-process"
shot "UI-10A-P1复核确认（教务）" "view=review-final"
