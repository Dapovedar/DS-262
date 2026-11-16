@echo off
chcp 65001 >nul
call compilar.bat
echo.
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out uci.PruebasSistema
echo.
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out uci.PruebasInterfaz
echo.
echo Para los tiempos de ejecucion: java -cp out uci.Benchmark
pause
