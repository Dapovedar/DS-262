@echo off
echo Restaurando la base de datos de ejemplo (50 pacientes, 5 funcionarios)...
xcopy /Y /E data-inicial\* data\ >nul
echo Listo.
pause
