# OrdenaDorJava - Organizador y Limpiador de PC

Aplicación de escritorio con GUI (JavaFX) para ordenar tus archivos, limpiar basura y optimizar el arranque de Windows en unos clics.

## 🧭 ¿Qué hace?
- Ordena carpetas grandes por tipo de archivo y genera reportes HTML.
- Limpia temporales (modo seguro) o hace mantenimiento profundo (modo agresivo) con tareas de red.
- Gestiona programas de inicio de Windows y permite desactivar Inicio Rápido (Fast Boot) para apagar por completo el equipo.
- Limpia caché de navegadores (Chrome, Edge, Firefox, Brave).
- Detecta duplicados por hash y muestra espacio recuperable.
- Analiza espacio en disco con gráficas y lista de archivos grandes.

## ⚙️ Cómo ejecutarlo (por sistema operativo)
- **Windows**: `java -jar target\OrdenDeArchivos-1.0-jar-with-dependencies.jar` o `ejecutar-gui.bat`. Para instalar Java 17: `winget install EclipseAdoptium.Temurin.17.JDK`. El tab de Optimización de Inicio solo aparece en Windows.
- **macOS**: instala Java 17 y OpenJFX (`brew install openjdk@17 openjfx`).
 Ejecuta con:  
  `java --module-path /opt/homebrew/opt/openjfx/lib --add-modules javafx.controls,javafx.fxml -jar target/OrdenDeArchivos-1.0-jar-with-dependencies.jar`
- **Linux (Debian/Ubuntu)**: `sudo apt-get install openjdk-17-jdk openjfx` y luego:  
  `java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -jar target/OrdenDeArchivos-1.0-jar-with-dependencies.jar`
- **Portable** (Windows): descomprime `OrdenaDor-Portable-v1.0.zip` y ejecuta `OrdenaDor.bat`.

## ▶️ Uso rápido
1) **Portable**: descomprime `OrdenaDor-Portable-v1.0.zip` y ejecuta `OrdenaDor.bat`.
2) **JAR**: `java -jar target\OrdenDeArchivos-1.0-jar-with-dependencies.jar` (o `ejecutar-gui.bat`).
3) **Consola**: `java -jar target\OrdenDeArchivos-1.0-jar-with-dependencies.jar --console` (o `ejecutar-consola.bat`).

## 🚀 Características Principales

### 📁 Organizador de Archivos
- Organiza automáticamente archivos por categorías (imágenes, documentos, música, videos, etc.)
- **NUEVO**: Personaliza el nombre de la carpeta destino (ej. `_ordenado` o tu propio nombre).
- **NUEVO**: Botón "Abrir" para ir directamente a la ubicación del archivo organizado.
- Generación de reportes HTML detallados

### 🧹 Limpiador del Sistema
- **Modo Seguro**: Limpia archivos temporales sin riesgos
- **Modo Agresivo**: Limpieza profunda con flush DNS y reset de red (requiere permisos)
- Compatible con Windows, macOS y Linux
- Muestra espacio liberado en tiempo real

### 🚀 Optimización de Inicio (NUEVO)
- **Gestor de Arranque**: Visualiza y elimina programas que inician automáticamente con Windows para acelerar el encendido.
- **Control de Inicio Rápido**: Desactiva el "Fast Boot" para asegurar un apagado completo y evitar problemas de caché persistente.

### 🌐 Limpiador de Navegadores
- Detecta y limpia caché de:
  - Google Chrome
  - Mozilla Firefox
  - Microsoft Edge
  - Brave Browser
- Escaneo previo para ver cuánto espacio se liberará
- Advertencias de seguridad antes de limpiar

### 🔍 Detector de Archivos Duplicados
- Encuentra archivos idénticos usando hash SHA-256
- Configurable por tamaño mínimo
- **NUEVO**: Botones para **Eliminar** y **Abrir Ubicación** directamente.
- Muestra espacio recuperable

### 💾 Analizador de Espacio en Disco
- Gráfico de torta con distribución por categorías
- Lista de archivos grandes (>100MB)
- **NUEVO**: Botones para **Eliminar** archivo y **Abrir Ubicación**.
- **Seguridad**: Protección para no borrar archivos del sistema accidentalmente.

## 📦 Instalación y Ejecución

### Requisitos
- Java 17 o superior
- Maven 3.6+ (solo para compilar desde código fuente)

### 🎯 Opción 1: Versión Portable (Recomendado para usuarios)

**Ya está lista para usar:**
1. Descarga `OrdenaDor-Portable-v1.0.zip`
2. Descomprime en cualquier carpeta
3. Ejecuta `OrdenaDor.bat`

**Ventajas:**
- ✅ No requiere instalación
- ✅ Puede ejecutarse desde USB
- ✅ Fácil de distribuir

### 🔧 Opción 2: Crear Instalador Profesional (.exe)

Para crear un instalador con asistente de instalación:

```bash
# 1. Instala WiX Toolset: https://wixtoolset.org/
# 2. Ejecuta el script:
crear-instalador.bat

# El instalador se creará en: installer/OrdenaDor-1.0.exe
```

**Ventajas del instalador:**
- ✅ Instalación profesional con asistente
- ✅ Acceso directo en menú de inicio
- ✅ Desinstalador automático
- ✅ No requiere Java en la PC del usuario

📖 **Guía completa**: Ver [GUIA-INSTALACION.md](GUIA-INSTALACION.md)

### 💻 Opción 3: Ejecutar desde JAR (Desarrolladores)

#### Ejecutar con Scripts

##### Modo Interfaz Gráfica (por defecto)
```bash
# Windows
ejecutar-gui.bat

# O directamente:
java -jar target\OrdenDeArchivos-1.0-jar-with-dependencies.jar
```

##### Modo Consola
```bash
# Windows
ejecutar-consola.bat

# O directamente:
java -jar target\OrdenDeArchivos-1.0-jar-with-dependencies.jar --console
```

#### Compilar desde el código fuente

```bash
# Compilar el proyecto
mvn clean package

# El JAR se generará en:
# target/OrdenDeArchivos-1.0-jar-with-dependencies.jar
```

## 📋 Categorías de Archivos

| Carpeta       | Tipo     | Extensiones Soportadas                       |
| :------------ | :------- | :------------------------------------------- |
| `ejecutables` | `string` | .exe, .sh, .deb, .msi, .bat, .cmd            |
| `imagenes`    | `string` | .jpg, .png, .svg, .gif, .bmp, .webp, .ico    |
| `documentos`  | `string` | .pdf, .docx, .xlsx, .txt, .odt, .pptx        |
| `musica`      | `string` | .mp3, .wav, .flac, .aac, .ogg, .m4a          |
| `videos`      | `string` | .mp4, .mkv, .avi, .mov, .wmv, .flv, .webm    |
| `archivos`    | `string` | .zip, .rar, .7z, .tar, .gz, .bz2             |
| `codigo`      | `string` | .java, .py, .js, .html, .css, .cpp, .c, .php |
| `otros`       | `string` | Archivos que no pertenecen a ninguna categoría específica |

## 🎨 Interfaz Gráfica

La aplicación cuenta con 6 pestañas principales:

1. **📊 Dashboard**: Información del sistema y accesos rápidos
2. **📁 Organizador**: Organiza archivos por categorías
3. **🧹 Limpiador Sistema**: Limpia archivos temporales y caché del sistema
4. **🌐 Limpiador Navegadores**: Limpia caché de navegadores web
5. **🔍 Archivos Duplicados**: Encuentra y elimina archivos duplicados
6. **💾 Análisis de Disco**: Visualiza el uso de espacio en disco

## ✨ Mejoras y Características Técnicas

✅ Independiente del sistema operativo usando `java.nio.file.Path`

✅ Carpeta de origen configurable por el usuario

✅ Evita mover el propio JAR o la carpeta de destino

✅ Ignora archivos ocultos y directorios del sistema

✅ Genera logs detallados (`log.txt`, `clean.log`) en la carpeta destino

✅ Soporta extensiones en mayúsculas/minúsculas (.JPG, .jpg)

✅ Código modular dividido en clases especializadas

✅ Interfaz gráfica moderna con tema oscuro

✅ Operaciones en segundo plano con barras de progreso

✅ Detección automática de navegadores instalados

✅ Hash SHA-256 para detección precisa de duplicados

## 📝 Estructura del Proyecto

```
OrdenaDorJava/
├── src/main/java/org/pcr/
│   ├── Main.java                    # Punto de entrada
│   ├── gui/
│   │   ├── MainApp.java             # Aplicación JavaFX principal
│   │   └── tabs/                    # Pestañas de la interfaz
│   │       ├── DashboardTab.java
│   │       ├── FileOrganizerTab.java
│   │       ├── SystemCleanerTab.java
│   │       ├── BrowserCleanerTab.java
│   │       ├── DuplicateFinderTab.java
│   │       └── DiskAnalyzerTab.java
│   ├── clean/                       # Funcionalidades de limpieza
│   │   ├── SystemCleaner.java
│   │   ├── BrowserCacheCleaner.java
│   │   ├── DuplicateFileFinder.java
│   │   └── DiskSpaceAnalyzer.java
│   ├── core/                        # Lógica principal
│   │   └── FileOrganizer.java
│   └── report/                      # Generación de reportes
│       └── HtmlReportWriter.java
├── src/main/resources/
│   └── styles.css                   # Tema oscuro moderno
├── pom.xml                          # Configuración Maven
├── ejecutar-gui.bat                 # Script para modo GUI
└── ejecutar-consola.bat             # Script para modo consola
```

## ⚠️ Notas Importantes

- **No mueve archivos que estén abiertos o bloqueados**
- **No borra archivos, solo los reorganiza** (excepto en limpieza de caché)
- Se recomienda **cerrar navegadores** antes de limpiar su caché
- El **modo agresivo** puede requerir permisos de administrador
- Los **archivos duplicados** solo se reportan, no se eliminan automáticamente

## 🔧 Tecnologías Utilizadas

- **Java 17**: Lenguaje de programación
- **JavaFX 21**: Framework para interfaz gráfica
- **Maven**: Gestión de dependencias y compilación
- **Apache Commons Codec**: Hashing SHA-256 para duplicados

## 📄 Licencia

Este proyecto es de código abierto. Siéntete libre de usarlo y modificarlo según tus necesidades.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Si encuentras algún error o tienes sugerencias de mejora, no dudes en crear un issue o pull request.

---

**Desarrollado con ❤️ para mantener tu PC limpio y organizado**
