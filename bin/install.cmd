@echo off
echo [INFO] install module.

cd %~dp0
cd ..
call mvn clean source:jar install -Dmaven.test.skip=true
cd bin
pause