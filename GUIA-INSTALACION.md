# Guía de Instalación y Distribución - OrdenaDor

## 📦 Opciones de Distribución

Tienes **3 opciones** para distribuir tu aplicación:

---

## Opción 1: Instalador Profesional (.exe) ⭐ RECOMENDADO

### Ventajas
- ✅ Instalación profesional con asistente
- ✅ Acceso directo en menú de inicio
- ✅ Desinstalador automático
- ✅ No requiere Java en la PC del usuario (incluye runtime)
- ✅ Icono personalizado

### Requisitos
- Java 17+ con jpackage
- WiX Toolset (para Windows): https://wixtoolset.org/

### Cómo crear el instalador

```bash
# 1. Ejecuta el script
crear-instalador.bat

# 2. El instalador se creará en:
installer/OrdenaDor-1.0.exe
```

### Resultado
- **Archivo**: `OrdenaDor-1.0.exe` (~50-60 MB)
- **Distribución**: Envía este archivo a los usuarios
- **Instalación**: Doble clic → Siguiente → Instalar

---

## Opción 2: Versión Portable (ZIP) 🎒

### Ventajas
- ✅ No requiere instalación
- ✅ Puede ejecutarse desde USB
- ✅ Fácil de distribuir
- ❌ Requiere Java 17+ instalado en la PC del usuario

### Cómo crear la versión portable

```bash
# 1. Ejecuta el script
crear-portable.bat

# 2. Se creará:
OrdenaDor-Portable-v1.0.zip
```

### Resultado
- **Archivo**: `OrdenaDor-Portable-v1.0.zip` (~10 MB)
- **Distribución**: Envía el ZIP
- **Uso**: Descomprimir → Ejecutar `OrdenaDor.bat`

---

## Opción 3: JAR Directo (Desarrolladores) 💻

### Ventajas
- ✅ Más ligero
- ✅ Multiplataforma (Windows, Mac, Linux)
- ❌ Requiere Java 17+ instalado
- ❌ Ejecución desde línea de comandos

### Cómo usar

```bash
# Ya está compilado en:
target/OrdenDeArchivos-1.0-jar-with-dependencies.jar

# Ejecutar con:
java -jar target/OrdenDeArchivos-1.0-jar-with-dependencies.jar
```

---

## 🔧 Instalación de Requisitos (para crear instalador)

### 1. Verificar Java 17+

```bash
java -version
```

Si no tienes Java 17+, descarga desde:
- **Adoptium (recomendado)**: https://adoptium.net/
- **Oracle JDK**: https://www.oracle.com/java/technologies/downloads/

### 2. Instalar WiX Toolset (solo Windows)

Para crear instaladores `.exe` en Windows:

1. Descarga WiX Toolset: https://wixtoolset.org/
2. Instala la versión 3.x (la más reciente)
3. Reinicia la terminal después de instalar

### 3. Verificar jpackage

```bash
jpackage --version
```

Si no está disponible, asegúrate de usar un JDK completo (no JRE).

---

## 📋 Comparación de Opciones

| Característica | Instalador .exe | Portable ZIP | JAR Directo |
|----------------|-----------------|--------------|-------------|
| Tamaño | ~50-60 MB | ~10 MB | ~10 MB |
| Requiere Java | ❌ No | ✅ Sí (17+) | ✅ Sí (17+) |
| Instalación | Asistente | Descomprimir | Ninguna |
| Menú inicio | ✅ Sí | ❌ No | ❌ No |
| Desinstalador | ✅ Sí | Manual | Manual |
| Icono | ✅ Sí | ✅ Sí | ❌ No |
| Profesional | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |

---

## 🎯 Recomendación por Caso de Uso

### Para usuarios finales (no técnicos)
👉 **Opción 1: Instalador .exe**
- Experiencia profesional
- No necesitan saber de Java
- Fácil de instalar y desinstalar

### Para distribución rápida
👉 **Opción 2: Portable ZIP**
- Más ligero para enviar
- No requiere permisos de administrador
- Ideal para pruebas

### Para desarrolladores
👉 **Opción 3: JAR Directo**
- Más flexible
- Multiplataforma
- Fácil de actualizar

---

## 🚀 Pasos para Distribución Profesional

### 1. Crear el instalador

```bash
crear-instalador.bat
```

### 2. Probar el instalador

- Ejecuta `installer/OrdenaDor-1.0.exe`
- Instala en una carpeta de prueba
- Verifica que funciona correctamente
- Desinstala desde Panel de Control

### 3. Distribuir

**Opciones de distribución:**

- **Email**: Envía el `.exe` directamente
- **Google Drive / Dropbox**: Sube y comparte el enlace
- **GitHub Releases**: Crea un release con el instalador
- **Sitio web**: Ofrece descarga directa

### 4. Documentación para usuarios

Incluye estas instrucciones:

```
INSTALACIÓN DE ORDENADOR

1. Descarga OrdenaDor-1.0.exe
2. Doble clic en el archivo
3. Sigue el asistente de instalación
4. Busca "OrdenaDor" en el menú de inicio

REQUISITOS:
- Windows 10 o superior
- 100 MB de espacio en disco
- No requiere Java (incluido)

FUNCIONALIDADES:
✓ Organizar archivos por categorías
✓ Limpiar archivos temporales
✓ Limpiar caché de navegadores
✓ Encontrar archivos duplicados
✓ Analizar espacio en disco
```

---

## ⚠️ Notas Importantes

### Antivirus
Algunos antivirus pueden marcar el instalador como sospechoso la primera vez. Esto es normal para aplicaciones nuevas. Soluciones:

1. **Firma digital**: Compra un certificado de firma de código (~$100-300/año)
2. **Whitelist**: Reporta el archivo a antivirus populares
3. **Documentación**: Explica que es un falso positivo

### Actualizaciones
Para crear una nueva versión:

1. Actualiza la versión en `pom.xml`
2. Recompila: `mvn clean package`
3. Crea nuevo instalador: `crear-instalador.bat`
4. Distribuye la nueva versión

### Licencia
Considera agregar un archivo `LICENSE.txt` con los términos de uso.

---

## 📞 Soporte

Si tienes problemas creando el instalador:

1. Verifica que tienes Java 17+ con jpackage
2. Instala WiX Toolset
3. Ejecuta `crear-portable.bat` como alternativa
4. Revisa los logs de error

---

## ✅ Checklist de Distribución

- [ ] Compilar proyecto: `mvn clean package`
- [ ] Probar JAR: `ejecutar-gui.bat`
- [ ] Crear instalador: `crear-instalador.bat`
- [ ] Probar instalación completa
- [ ] Crear documentación de usuario
- [ ] Preparar capturas de pantalla
- [ ] Subir a plataforma de distribución
- [ ] Compartir enlace de descarga

---

**¡Tu aplicación está lista para distribuirse profesionalmente! 🎉**
