@echo off
chcp 65001 >nul
if not exist out call compilar.bat
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out uci.Main
