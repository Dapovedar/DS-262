@echo off
chcp 65001 >nul
echo Compilando...
if exist out rmdir /s /q out
mkdir out
javac -encoding UTF-8 -d out src\uci\*.java src\uci\modelo\*.java src\uci\estructuras\*.java src\uci\servicio\*.java src\uci\persistencia\*.java src\uci\ui\*.java pruebas\uci\*.java
if errorlevel 1 (
  echo ERROR de compilacion. Verifique que javac este instalado ^(JDK 17 o superior^).
  pause
  exit /b 1
)
echo Compilacion exitosa.
