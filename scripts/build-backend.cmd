@echo off
setlocal
cd /d "%~dp0.."
echo Building backend with the default Maven repository: %USERPROFILE%\.m2\repository
call mvn clean package %*
exit /b %ERRORLEVEL%
