@echo off
REM Ejecutar OrdenaDor en modo CONSOLA
echo ========================================
echo   OrdenaDor - Modo Consola
echo ========================================
echo.

java -jar target\OrdenDeArchivos-1.0-jar-with-dependencies.jar --console

if errorlevel 1 (
    echo.
    echo ERROR: No se pudo iniciar la aplicacion.
    echo Verifica que tengas Java 17 o superior instalado.
    echo.
    pause
)
