@echo off
echo ========================================
echo   CREANDO INSTALADOR NATIVO (SIN JAVA REQUERIDO)
echo   Usando jpackage del JDK actual
echo ========================================

rem 1. Limpiar dist anterior
if exist dist rd /s /q dist
if exist "OrdenaDor-Setup.zip" del "OrdenaDor-Setup.zip"

rem 2. Crear Imagen de Aplicación (Carpeta con EXE y JRE embebido)
echo.
echo [1/3] Generando imagen de la aplicacion...
echo Esto puede tardar unos minutos...

rem Intentamos con icono, si falla se vera el de java
jpackage ^
  --type app-image ^
  --dest dist ^
  --name "OrdenaDor" ^
  --input target ^
  --main-jar "OrdenDeArchivos-1.0-jar-with-dependencies.jar" ^
  --main-class org.pcr.gui.MainApp ^
  --icon "src\main\resources\images\logo.png" ^
  --java-options "-Dfile.encoding=UTF-8 -Xms256m -Xmx1024m" ^
  --vendor "Estirp3" ^
  --app-version 1.0.0

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] jpackage fallo con icono. Intentando sin icono...
    jpackage ^
      --type app-image ^
      --dest dist ^
      --name "OrdenaDor" ^
      --input target ^
      --main-jar "OrdenDeArchivos-1.0-jar-with-dependencies.jar" ^
      --main-class org.pcr.gui.MainApp ^
      --java-options "-Dfile.encoding=UTF-8 -Xms256m -Xmx1024m" ^
      --vendor "Estirp3"
)

rem 3. Crear ZIP distribuible
echo.
echo [2/3] Comprimiendo para distribucion...
powershell Compress-Archive -Path "dist\OrdenaDor" -DestinationPath "OrdenaDor-Setup.zip" -Force

echo.
echo ========================================
echo   ¡HECHO!
echo ========================================
echo.
echo Archivo listo para enviar: OrdenaDor-Setup.zip
echo (Contiene OrdenaDor.exe y todo lo necesario para correr sin Java)
echo.
pause
