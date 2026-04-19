#!/bin/bash
echo "================================================"
echo " OS Thread Simulator - Build Script (Linux/Mac)"
echo "================================================"

command -v javac >/dev/null 2>&1 || { echo "javac not found. Install JDK 11+"; exit 1; }

rm -rf out && mkdir out

find src -name "*.java" > sources.txt
javac -d out @sources.txt
if [ $? -ne 0 ]; then echo "COMPILATION FAILED."; rm -f sources.txt; exit 1; fi
rm -f sources.txt

printf "Main-Class: com.ossim.Main\n\n" > manifest.txt
jar cfm OSThreadSim.jar manifest.txt -C out .
rm -f manifest.txt

echo ""
echo "================================================"
echo " BUILD SUCCESSFUL!  Run:  java -jar OSThreadSim.jar"
echo "================================================"
