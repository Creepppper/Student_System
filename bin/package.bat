@echo off
echo.
echo [信息] 打包Web工程，生成war/jar包文件。
echo.

call "%~dp0..\scripts\build-backend.cmd" %*

pause
