# Dev-QA Microservicio de Productos

API REST de productos desarrollada con Java 17, Spring Boot 3.5.7 y MongoDB. Incluye validación, respuestas JSON de error, pruebas MVC/unitarias y pruebas de persistencia con Testcontainers.

## Requisitos

- JDK 17 y Maven Wrapper (`mvnw.cmd`).
- MongoDB Community 7 o Docker Desktop con Docker Compose v2.
- Para las pruebas de integración con Testcontainers: Docker operativo.
- SonarQube Server accesible y un token autorizado para verificar un Quality Gate.

## MongoDB y ejecución local

El repositorio no incluye credenciales. Para Docker:

1. Copia `.env.example` a `.env` y sustituye la contraseña de ejemplo por una contraseña local fuerte.
2. Ejecuta `docker compose up -d mongodb`.
3. Configura la URI de la aplicación con el mismo usuario/contraseña.

PowerShell:

```powershell
$env:MONGODB_URI = "mongodb://devqa:CONTRASENA_LOCAL@localhost:27017/devqa_productos?authSource=admin"
.\mvnw.cmd spring-boot:run
```

Git Bash:

```bash
export MONGODB_URI='mongodb://devqa:CONTRASENA_LOCAL@localhost:27017/devqa_productos?authSource=admin'
./mvnw spring-boot:run
```

Para una instalación local sin autenticación, el valor predeterminado es `mongodb://localhost:27017/devqa_productos`. La variable `MONGODB_URI` prevalece sobre ese valor. Los índices se crean automáticamente al iniciar; el índice único de `codigo` protege también frente a escrituras concurrentes.

Con MongoDB disponible, se puede comprobar la conexión y escribir/leer un registro:

```bash
mongosh "$MONGODB_URI" --eval 'db.runCommand({ping: 1}); db.verificacion.insertOne({ok: true}); db.verificacion.findOne({ok: true})'
```

La conectividad de MongoDB no quedó verificada en el entorno de elaboración: `localhost:27017` rechazó la conexión y Docker no está instalado. No se instaló un servidor ni se simuló una persistencia exitosa.

## API

Base URL: `http://localhost:8080/api/productos`.

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/productos` | 200, colección |
| GET | `/api/productos/{id}` | 200 o 404 |
| POST | `/api/productos` | 201 y encabezado `Location`; 400 o 409 |
| PUT | `/api/productos/{id}` | 200; 400, 404 o 409 |
| DELETE | `/api/productos/{id}` | 204 o 404 |

Ejemplo POST:

```json
{
  "nombre": "Teclado",
  "codigo": "TEC-001",
  "precio": 899.0,
  "stock": 5
}
```

`nombre` y `codigo` no pueden estar vacíos; `precio` debe ser positivo y `stock` mayor o igual a cero. `codigo` debe ser único. Los errores se devuelven como JSON con `timestamp`, `status`, `error`, `message`, `path` y `details`; no se exponen trazas ni mensajes del driver.

## Pruebas y cobertura

```powershell
.\mvnw.cmd verify
```

La suite incluye Mockito/AssertJ para servicio y MockMvc para endpoints, validación y errores. La prueba `ProductoMongoIntegrationTest` usa Testcontainers con MongoDB real y está configurada para omitirse explícitamente cuando Docker no está disponible. Si se tiene Docker, `.\mvnw.cmd verify` la ejecuta automáticamente.

JaCoCo genera `target/site/jacoco/index.html` y `target/site/jacoco/jacoco.xml`; `verify` falla si la cobertura del bundle cae por debajo del 80% de líneas o ramas.

## SonarQube

Se configuraron el scanner Maven, el identificador `com.practicacarlos:devqa-microservicio` y la ruta del XML de JaCoCo. No se detectaron credenciales `SONAR_TOKEN` ni una instancia accesible configurada, por lo que no se declara un Quality Gate.

Con una instancia SonarQube autorizada y `SONAR_TOKEN` disponible como variable de entorno:

```powershell
.\mvnw.cmd clean verify sonar:sonar "-Dsonar.host.url=http://localhost:9000"
```

Después del análisis, consulta el proyecto en `http://localhost:9000` y espera a que el Quality Gate reporte su estado. Nunca pegues el token en el comando, el repositorio o este README.

## Evidencias y bloqueos del entorno

Los comandos y resultados efectivamente obtenidos se guardan en [`evidencias/`](evidencias/). Las pruebas HTTP con MockMvc sí ejecutan el stack MVC, pero usan un servicio simulado; no equivalen a persistencia MongoDB. La prueba de integración real permanece pendiente de Docker/MongoDB. Las peticiones que necesitan la base de datos no se presentan como capturas exitosas.

## Solución de problemas

- `ECONNREFUSED localhost:27017`: inicia MongoDB o Docker Compose y verifica el puerto con `mongosh`.
- Autenticación rechazada: confirma que `.env` y `MONGODB_URI` tengan el mismo usuario y contraseña; no cambies el volumen para intentar reparar datos existentes.
- Testcontainers indica que Docker no está disponible: inicia Docker Desktop y repite `.\mvnw.cmd verify`.
- No se obtiene Quality Gate: configura la URL de SonarQube y `SONAR_TOKEN` con acceso autorizado.

## Referencias

- [Spring Boot 3.5 Reference](https://docs.spring.io/spring-boot/3.5/reference/)
- [Spring Data MongoDB](https://docs.spring.io/spring-data/mongodb/reference/)
- [Testcontainers MongoDB](https://java.testcontainers.org/modules/databases/mongodb/)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [SonarScanner for Maven](https://docs.sonarsource.com/sonarqube-server/analyzing-source-code/scanners/sonarscanner-for-maven/)
