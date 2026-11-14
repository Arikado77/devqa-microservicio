# Evidencias de la práctica

## Resultados reproducibles

- `maven-verify.log`: salida íntegra del último `mvnw verify` completado con `BUILD SUCCESS`.
- `clean-verify.log`: salida del intento solicitado `mvnw clean verify`; la fase `clean` se bloqueó al eliminar `target\classes`.
- `jacoco.csv`: exportación CSV del reporte real de JaCoCo.
- `resultado-pruebas.txt`: resumen de pruebas aprobado/omitido extraído de Surefire.
- `sonarqube.txt`: estado de conectividad observado para el intento local de SonarQube.

## Límites de la evidencia HTTP

`ProductoControllerTest` ejecuta y valida solicitudes con MockMvc: endpoints CRUD, validación 400, recurso inexistente 404 y conflictos 409. Son comprobaciones del stack MVC con el servicio simulado, no solicitudes de red ni evidencia de persistencia.

No se anexan capturas Postman/cURL de CRUD contra la aplicación en ejecución: Docker no está instalado, no existe `mongod`/servicio MongoDB y `localhost:27017` rechazó conexiones. La prueba MongoDB con Testcontainers quedó omitida por falta de Docker. Tampoco se fabricaron respuestas HTTP ni imágenes.

## SonarQube

No había servicio local en `localhost:9000` ni variable de entorno `SONAR_TOKEN`. No se ejecutó ni se declara un Quality Gate. El informe técnico y README indican el comando necesario para completarlo con credenciales autorizadas.
