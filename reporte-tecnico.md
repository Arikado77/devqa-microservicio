# Universidad Tecnológica de Chihuahua

## Práctica 2 Dev-QA
### Microservicio REST de productos con Spring Boot y MongoDB

**Alumno:** Carlos Arikado Márquez Carlos Eduardo  
**Fecha:** 13 de noviembre de 2026

## 1. Resumen

Se implementó una API REST para administrar productos con Java 17 y Spring Boot 3.5.7. El sistema incluye validaciones Jakarta, persistencia MongoDB mediante Spring Data, índice único por código, respuestas de error JSON y pruebas unitarias y MVC.

La aplicación y pruebas locales compilaron. Maven `verify` terminó en `BUILD SUCCESS`; JaCoCo reportó 92.55% de líneas y 100% de ramas. La integración que requiere una base MongoDB real quedó bloqueada por la ausencia de Docker y de un servidor MongoDB local. El análisis SonarQube/Quality Gate no se pudo ejecutar por falta de servidor y autenticación.

## 2. Arquitectura

`ProductoController` expone HTTP y valida el cuerpo de las solicitudes; `ProductoService` aplica las reglas de negocio y traduce las carreras de unicidad; `ProductoRepository` abstrae MongoDB. El modelo `Producto` se mapea a la colección `productos`. `GlobalExceptionHandler` genera un contrato de error común sin divulgar trazas.

**Tecnologías:** Java 17.0.11, Maven Wrapper 3.10.0, Spring Boot 3.5.7, Spring MVC, Spring Data MongoDB, Jakarta Validation, JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers y JaCoCo 0.8.13.

## 3. Contrato HTTP

| Operación | Resultado esperado |
|---|---|
| `GET /api/productos` | 200 con colección |
| `GET /api/productos/{id}` | 200 o 404 |
| `POST /api/productos` | 201 y `Location`; 400 por validación o 409 por duplicado |
| `PUT /api/productos/{id}` | 200; 400, 404 o 409 |
| `DELETE /api/productos/{id}` | 204 o 404 |

Ejemplo válido:

```json
{"nombre":"Teclado","codigo":"TEC-001","precio":899.0,"stock":5}
```

Nombre y código son obligatorios; el precio debe ser mayor que cero y el stock no negativo. El índice MongoDB único para `codigo` protege las escrituras concurrentes.

Los errores siguen el formato `{ "timestamp", "status", "error", "message", "path", "details" }`. Los detalles de validación son deterministas y no se devuelve información interna del driver.

## 4. Pruebas y cobertura

Comando ejecutado: `.\mvnw.cmd -B -ntp verify`.

`.\mvnw.cmd clean verify` también se intentó; la fase `clean` no pudo eliminar `target\classes` por un bloqueo de archivos reportado por Windows. El `verify` posterior completó correctamente desde el mismo workspace. El fallo de limpieza se conserva en `evidencias/clean-verify.log`.

| Grupo | Ejecutadas | Aprobadas | Omitidas |
|---|---:|---:|---:|
| ProductoControllerTest (MockMvc) | 11 | 11 | 0 |
| ProductoServiceTest (Mockito) | 12 | 12 | 0 |
| ProductoMongoIntegrationTest (Testcontainers) | 1 | 0 | 1 |
| **Total** | **24** | **23** | **1** |

JaCoCo (bundle de ocho clases de producción): **87/94 líneas = 92.55%** y **8/8 ramas = 100%**. El umbral configurado es 80% para ambas métricas y el check pasó. Reporte HTML: `target/site/jacoco/index.html`; XML: `target/site/jacoco/jacoco.xml`.

MockMvc verifica el stack MVC y los estados CRUD/400/404/409 con el servicio simulado. No es una prueba de persistencia. La prueba de integración ejecutable con MongoDB real está implementada, pero Testcontainers la omitió porque el entorno no tiene Docker.

## 5. Persistencia y verificación ambiental

La URI se externaliza con `MONGODB_URI`; el valor local predeterminado es `mongodb://localhost:27017/devqa_productos`. `docker-compose.yml` declara MongoDB 7.0.14 y volumen persistente, con usuario/contraseña suministrados desde `.env`, que está excluido de Git.

La comprobación real con `mongosh` contra `localhost:27017` falló con `ECONNREFUSED`. No hay servicio MongoDB local ni binario `mongod` disponible. Por tanto, no se afirma que haya ocurrido una lectura/escritura real.

## 6. Calidad estática

JaCoCo se generó y verificó. SonarQube no se analizó: `localhost:9000` no respondió y no había `SONAR_TOKEN`. **Quality Gate: sin resultado**, no aprobado ni fallido. El scanner Maven y `sonar.coverage.jacoco.xmlReportPaths` están configurados. Para completar, iniciar una instancia autorizada, establecer `SONAR_TOKEN` fuera del repositorio y ejecutar:

```powershell
.\mvnw.cmd clean verify sonar:sonar "-Dsonar.host.url=http://localhost:9000"
```

## 7. Evidencias

`evidencias/` conserva la salida de Maven, resultados Surefire, CSV JaCoCo y el bloqueo SonarQube. No se fabricaron capturas. Las capturas de Postman/cURL contra una aplicación respaldada por MongoDB quedan pendientes hasta disponer del servidor; los estados MVC sí se verifican en los tests automatizados.

## 8. Conclusión

La implementación y la verificación unitaria/MVC satisfacen el contrato funcional y superan el umbral de cobertura configurado. La verificación de persistencia real, las capturas HTTP de extremo a extremo y el Quality Gate requieren infraestructura no disponible en este equipo. Esos bloqueos se registran explícitamente para no confundir pruebas simuladas con una integración real.

## Referencias

- Spring Boot 3.5: https://docs.spring.io/spring-boot/3.5/reference/
- Spring Data MongoDB: https://docs.spring.io/spring-data/mongodb/reference/
- Testcontainers MongoDB: https://java.testcontainers.org/modules/databases/mongodb/
- JaCoCo Maven: https://www.jacoco.org/jacoco/trunk/doc/maven.html
- SonarScanner Maven: https://docs.sonarsource.com/sonarqube-server/analyzing-source-code/scanners/sonarscanner-for-maven/
