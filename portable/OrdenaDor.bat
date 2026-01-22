@echo off
REM OrdenaDor - Organizador y Limpiador de PC

REM Verificar Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java no esta instalado
    echo.
    echo Descarga Java 17 desde:
    echo https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Ejecutar aplicacion
start javaw -jar lib\OrdenaDor.jar
