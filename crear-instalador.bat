@echo off
REM ============================================
REM  Script para crear instalador de OrdenaDor
REM ============================================

echo.
echo ========================================
echo   Creando Instalador de OrdenaDor
echo ========================================
echo.

REM Verificar que existe el JAR
if not exist "target\OrdenDeArchivos-1.0-jar-with-dependencies.jar" (
    echo ERROR: No se encuentra el JAR compilado.
    echo Por favor ejecuta primero: mvn clean package
    echo.
    pause
    exit /b 1
)

REM Crear carpeta para el instalador
if not exist "installer" mkdir installer

echo [1/4] Verificando Java 17+...
java -version 2>&1 | findstr /C:"17" /C:"18" /C:"19" /C:"20" /C:"21" >nul
if errorlevel 1 (
    echo ADVERTENCIA: Se recomienda Java 17 o superior para jpackage
)

echo [2/4] Preparando archivos...

REM Crear carpeta temporal para input
if exist "temp-jpackage" rmdir /s /q temp-jpackage
mkdir temp-jpackage
copy "target\OrdenDeArchivos-1.0-jar-with-dependencies.jar" "temp-jpackage\" >nul

echo [3/4] Generando instalador Windows (.exe)...
echo.
echo Esto puede tomar varios minutos...
echo.

jpackage ^
  --type exe ^
  --input temp-jpackage ^
  --name "OrdenaDor" ^
  --main-jar OrdenDeArchivos-1.0-jar-with-dependencies.jar ^
  --main-class org.pcr.Main ^
  --dest installer ^
  --app-version 1.0 ^
  --vendor "OrdenaDor Team" ^
  --description "Organizador de archivos y limpiador de PC" ^
  --icon src\main\resources\icon.png ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut ^
  --win-menu-group "OrdenaDor" ^
  --java-options "--add-opens javafx.graphics/javafx.css=ALL-UNNAMED"

if errorlevel 1 (
    echo.
    echo ERROR: Fallo al crear el instalador.
    echo.
    echo Posibles soluciones:
    echo 1. Verifica que tienes Java 17+ con jpackage incluido
    echo 2. Si usas OpenJDK, asegúrate de tener jpackage disponible
    echo 3. Instala WiX Toolset para crear instaladores .exe en Windows
    echo    Descarga: https://wixtoolset.org/
    echo.
    pause
    exit /b 1
)

echo [4/4] Limpiando archivos temporales...
rmdir /s /q temp-jpackage

echo.
echo ========================================
echo   INSTALADOR CREADO EXITOSAMENTE
echo ========================================
echo.
echo El instalador se encuentra en:
echo   installer\OrdenaDor-1.0.exe
echo.
echo Tamaño aproximado: ~50-60 MB
echo.
echo Puedes distribuir este archivo .exe
echo No requiere Java instalado en la PC destino
echo.
pause
