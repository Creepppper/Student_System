@echo off
setlocal
set "PROJECT_ROOT=%~dp0.."
if not defined DB_PASSWORD (
  echo DB_PASSWORD is required. Set it in the current terminal without putting it in this script.
  exit /b 1
)
if not defined TOKEN_SECRET (
  echo TOKEN_SECRET is required. Set it in the current terminal without putting it in this script.
  exit /b 1
)
if not defined LOG_PATH set "LOG_PATH=%PROJECT_ROOT%\logs"
if not defined RUOYI_PROFILE set "RUOYI_PROFILE=%PROJECT_ROOT%\data\uploadPath"
if not defined SERVER_PORT set "SERVER_PORT=8080"
if not exist "%PROJECT_ROOT%\ruoyi-admin\target\ruoyi-admin.jar" (
  echo Backend JAR is missing. Run scripts\build-backend.cmd first.
  exit /b 1
)
if not exist "%LOG_PATH%" mkdir "%LOG_PATH%"
if not exist "%RUOYI_PROFILE%" mkdir "%RUOYI_PROFILE%"
cd /d "%PROJECT_ROOT%"
java -Duser.timezone=Asia/Shanghai -jar "%PROJECT_ROOT%\ruoyi-admin\target\ruoyi-admin.jar"
exit /b %ERRORLEVEL%
