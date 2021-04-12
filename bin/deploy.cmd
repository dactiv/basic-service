@echo off
echo [INFO] deploy module.

cd %~dp0
cd ..
call mvn clean source:jar deploy
cd bin
pause