@echo off
call "%~dp0scripts\start-backend.cmd" %*
exit /b %ERRORLEVEL%
