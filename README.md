# OrdenaDorJava

### Ejecucion de para Crear JAR

#### Creacion de Jar

```maven
  mvn clean package
```

#### Si quieres incluir todas las dependencias en el JA
```maven
mvn clean compile assembly:single
```
#### Name item

```http
  OrdenDeArchivos-1.0-SNAPSHOT-jar-with-dependencies.jar
```

| Carpetas      | Type     | Description                       |
| :--------     | :------- | :-------------------------------- |
| `ejecutables` | `string` | Contiene archivos ejecutables como .exe, .sh, .deb. |   
| `imagenes`    | `string` | Contiene archivos de imágenes como .jpg, .png, .svg. |  
| `documentos`  | `string` | Contiene documentos como .pdf, .docx, .xlsx. |  
| `musica`      | `string` | Contiene archivos de música como .mp3, .wav, .flac. |   
| `videos`      | `string` | Contiene archivos de video como .mp4, .mkv, .avi. |   
| `archivos`    | `string` | Contiene archivos comprimidos como .zip, .rar, .7z. |   
| `otros`       | `string` | Contiene archivos que no pertenecen a ninguna categoría específica. |   

## Cambios y mejoras realizadas

✅ Independiente del sistema operativo usando java.nio.file.Path.

✅ Carpeta de origen configurable: el usuario puede escribirla o usar la carpeta actual.

✅ Evita mover el propio JAR o la carpeta de destino.

✅ Ignora archivos ocultos y directorios.

✅ Genera un log (log.txt) en la carpeta destino con detalle de:
 - Archivos movidos y su categoría
 - Archivos ignorados
 - Archivos bloqueados o en uso

✅ Soporta extensiones en mayúsculas/minúsculas (.JPG, .jpg).

✅ Código modular dividido en clases:

 - Main — Entrada del programa
 - DownloadDirResolver — Pregunta y valida la ruta
 - Config — Categorías y extensiones
 - CategoryResolver — Determina la categoría por extensión
 - AppLocation — Detecta ubicación del propio JAR
 - FileOrganizer — Lógica de organización y logging

```pgsql
Movido : foto1.jpg
Ignorado (propio JAR): OrdenDeArchivos-1.0-SNAPSHOT-jar-with-dependencies.jar
No movido (en uso/bloqueado): video.mp4
```

###  Notas importantes
 - No mueve archivos que estén abiertos o bloqueados.
 - No borra archivos, solo los reorganiza.
 - Se recomienda cerrar los programas que usen los archivos antes de ordenar.