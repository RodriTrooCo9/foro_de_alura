# Foro Alura — API

API REST con Java 17+, Spring Boot 3.1.4, Maven y MySQL. No incluye frontend; Swagger permite explorar los endpoints.

## Ejecutar

Instalar JDK 17 y Maven. Crear previamente una base MySQL `foro_alura_bd` y un usuario con permisos para crear/actualizar tablas en ella.

Desde este directorio:

```sh
export JAVA_HOME=/ruta/al/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
export DB_URL=jdbc:mysql://localhost:3306/foro_alura_bd
export DB_USER=foro
export DB_PASSWORD='contraseña-de-tu-base'
export JWT_SECRET='secreto-aleatorio-largo'
mvn test
mvn spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui/index.html

Registrar con `POST /usuarios` (`username`, `password`, `nombre`, `apellido`, `email`), iniciar sesión con `POST /login` (`username`, `password`) y usar el campo `JWTtoken` como `Authorization: Bearer TOKEN`. Recursos: `/usuarios`, `/cursos`, `/topicos`, `/respuestas`.

El test `contextLoads` requiere MySQL y las variables anteriores. Las pruebas de regresión de seguridad pueden ejecutarse sin base con `mvn -Dtest=SecurityRegressionTest test`.

Los scripts `mvnw` originales no incluyen `.mvn/wrapper`; usar Maven instalado. `ddl-auto=update` modifica el esquema: utilizar una base de desarrollo.

Ver `REVISION.md` para resultados y limitaciones de la revisión.
