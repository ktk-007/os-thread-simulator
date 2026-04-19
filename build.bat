@echo off
echo ================================================
echo  OS Thread Simulator - Build Script
echo ================================================
echo.

java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found. Install JDK 11+ from https://adoptium.net
    pause & exit /b 1
)
javac -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: javac not found. Install full JDK, not just JRE.
    pause & exit /b 1
)

echo Cleaning previous build...
if exist out rmdir /s /q out
mkdir out

echo Compiling sources...
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
if %errorlevel% neq 0 (
    echo COMPILATION FAILED.
    del sources.txt & pause & exit /b 1
)
del sources.txt

echo Creating JAR...
echo Main-Class: com.ossim.Main> manifest.txt
echo.>> manifest.txt
jar cfm OSThreadSim.jar manifest.txt -C out .
del manifest.txt

echo.
echo ================================================
echo  BUILD SUCCESSFUL!  Run:  java -jar OSThreadSim.jar
echo ================================================
pause
