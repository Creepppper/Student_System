@echo off
setlocal
set "PROJECT_ROOT=%~dp0.."
if not exist "%PROJECT_ROOT%\frontend\node_modules" (
  echo Frontend dependencies are missing. Run npm install in the frontend directory first.
  exit /b 1
)
cd /d "%PROJECT_ROOT%\frontend"
call npm run dev -- --strictPort %*
exit /b %ERRORLEVEL%
