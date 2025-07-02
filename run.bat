@echo off
echo Compiling Java files...
javac -cp "mysql-connector-j-9.2.0.jar" src/*.java -d bin

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo Starting application...
java -cp "mysql-connector-j-9.2.0.jar;bin" AttendanceManagementSystem

pause 