@echo off
REM Ejecutar OrdenaDor en modo GUI
echo ========================================
echo   OrdenaDor - Organizador y Limpiador
echo ========================================
echo.
echo Iniciando interfaz grafica...
echo.

java -jar target\OrdenDeArchivos-1.0-jar-with-dependencies.jar

if errorlevel 1 (
    echo.
    echo ERROR: No se pudo iniciar la aplicacion.
    echo Verifica que tengas Java 17 o superior instalado.
    echo.
    pause
)
