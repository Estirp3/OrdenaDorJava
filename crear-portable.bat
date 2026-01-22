@echo off
REM ============================================
REM  Alternativa: Crear ejecutable portable
REM  (No requiere instalación, solo un .exe)
REM ============================================

echo.
echo ========================================
echo   Creando Ejecutable Portable
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

REM Crear carpeta portable
if not exist "portable" mkdir portable
if not exist "portable\lib" mkdir portable\lib

echo [1/3] Copiando archivos...
copy "target\OrdenDeArchivos-1.0-jar-with-dependencies.jar" "portable\lib\OrdenaDor.jar" >nul
copy "src\main\resources\icon.png" "portable\" >nul 2>nul

echo [2/3] Creando launcher...

REM Crear script de inicio simple
(
echo @echo off
echo REM OrdenaDor - Organizador y Limpiador de PC
echo.
echo REM Verificar Java
echo java -version ^>nul 2^>^&1
echo if errorlevel 1 ^(
echo     echo ERROR: Java no esta instalado
echo     echo.
echo     echo Descarga Java 17 desde:
echo     echo https://adoptium.net/
echo     echo.
echo     pause
echo     exit /b 1
echo ^)
echo.
echo REM Ejecutar aplicacion
echo start javaw -jar lib\OrdenaDor.jar
) > "portable\OrdenaDor.bat"

REM Crear README
(
echo # OrdenaDor - Version Portable
echo.
echo ## Como usar:
echo.
echo 1. Ejecuta OrdenaDor.bat
echo 2. La aplicacion se abrira automaticamente
echo.
echo ## Requisitos:
echo.
echo - Java 17 o superior instalado
echo - Si no tienes Java, descargalo desde: https://adoptium.net/
echo.
echo ## Contenido:
echo.
echo - OrdenaDor.bat: Ejecutable principal
echo - lib/OrdenaDor.jar: Aplicacion
echo - icon.png: Icono de la aplicacion
echo.
echo ## Funcionalidades:
echo.
echo - Organizador de archivos por categorias
echo - Limpiador del sistema
echo - Limpiador de cache de navegadores
echo - Detector de archivos duplicados
echo - Analizador de espacio en disco
) > "portable\README.txt"

echo [3/3] Creando archivo ZIP...

REM Crear ZIP si PowerShell esta disponible
powershell -Command "Compress-Archive -Path 'portable\*' -DestinationPath 'OrdenaDor-Portable-v1.0.zip' -Force" 2>nul

if exist "OrdenaDor-Portable-v1.0.zip" (
    echo.
    echo ========================================
    echo   VERSION PORTABLE CREADA
    echo ========================================
    echo.
    echo Carpeta: portable\
    echo ZIP: OrdenaDor-Portable-v1.0.zip
    echo.
    echo Puedes distribuir el ZIP o la carpeta completa
    echo El usuario solo necesita ejecutar OrdenaDor.bat
    echo.
) else (
    echo.
    echo ========================================
    echo   VERSION PORTABLE CREADA
    echo ========================================
    echo.
    echo Carpeta: portable\
    echo.
    echo No se pudo crear el ZIP automaticamente
    echo Comprime manualmente la carpeta 'portable' si lo deseas
    echo.
)

pause
