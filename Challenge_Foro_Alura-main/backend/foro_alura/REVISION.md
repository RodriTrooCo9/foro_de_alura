# Revisión del proyecto — 2026-10-30

## Alcance y resultado

Revisados estructura, POM, configuración, los cinco controladores, entidades/DTO, repositorios, validadores, seguridad JWT, manejo de errores y la prueba existente. Es un backend REST; no hay frontend ni migraciones de base versionadas.

**Compilación correcta; cuatro regresiones correctas; arranque intentado y bloqueado por el entorno.** No se verificaron operaciones HTTP ni persistencia reales. Esto no constituye una certificación de seguridad ni una validación integral de negocio.

## Cambios realizados

- Eliminada dependencia `med.voll:api`, ajena al código y no distribuida con el proyecto.
- Registro público corregido de `/users` a `/usuarios`.
- Inyección de `PasswordEncoder` alineada con el tipo del bean.
- Location del usuario creado apunta ahora a su consulta real.
- Ruta de respuestas por usuario alineada con `usuarioId`.
- Actualizaciones parciales de respuestas toleran `solucion=null`.
- Tokens inválidos/caducados devuelven 401 y usuarios inexistentes/deshabilitados no se autentican mediante token.
- Caducidad calculada con Instant, sin depender del huso horario del servidor.
- Búsqueda de usuario para login informa UsernameNotFoundException cuando no existe.
- Validación Jakarta actualizada en curso; `topicoId` cambiado a Long para que NotNull detecte su ausencia.
- Credenciales MySQL y secreto JWT obtenidos mediante variables obligatorias. Se retiraron los valores inseguros incluidos originalmente.
- Añadidas instrucciones de ejecución y cuatro pruebas de regresión de seguridad.

## Verificación realizada

El Java del PATH es 11 y no hay Maven instalado. Los scripts mvnw carecen de `.mvn/wrapper`. Se utilizó el JDK 21 disponible en `/opt/nexus/jdk/temurin_21.0.9_10_linux_x86_64/jdk-21.0.9+10`, `javac --release 17`, Lombok y 136 JAR existentes en la caché local Maven. Compilaron todas las fuentes principales y de prueba. Esta alternativa no verifica la resolución del POM ni sustituye `mvn test`.

Ejecutadas las cuatro pruebas JUnit de SecurityRegressionTest mediante un invocador local de sus métodos @Test (sin motor Maven/Surefire): emisión/lectura y duración del token, rechazo de tokens malformados/caducados, respuesta 401 del filtro y actualización parcial sin solución. Resultado: 4/4.

El arranque con la configuración original alcanzó Spring Boot 3.1.4 y falló al abrir conexión JDBC: `Communications link failure`, causado por `java.net.SocketException: Operation not permitted`. Log: `/tmp/foro-arranque.log`. El cliente mysql tampoco pudo crear un socket. Docker no es accesible y Maven Central no resuelve por DNS desde este entorno. No se dejó un servidor activo. Para reproducir con la configuración final se necesitan DB_PASSWORD y JWT_SECRET, según README.

## Hallazgos pendientes

1. **Alta — autorización por recurso ausente.** SecurityConfigurations exige autenticación, pero los controladores no restringen por propietario o rol. Un usuario autenticado puede modificar/deshabilitar otros usuarios, cambiar roles y escribir recursos ajenos. Crear tópicos/respuestas acepta usuarioId suministrado por el cliente. Debe definirse e implementarse una política de permisos antes de exponer la API.
2. **Alta — integridad concurrente.** UsuarioDuplicado y TopicoDuplicado consultan antes de insertar; no se observan restricciones únicas de esquema para respaldar esas reglas. SolucionDuplicada verifica estado sin bloqueo ni restricción que garantice una única solución concurrente.
3. **Media — consulta de solución incorrecta.** TopicoController usa getReferenceByTopicoId sin filtrar solucion=true ni borrado=false. Con varias respuestas puede fallar o devolver una respuesta que no es solución.
4. **Media — estado de soluciones inconsistente.** Desmarcar o borrar una solución no reabre automáticamente el tópico; marcar de nuevo una solución ya seleccionada se rechaza como duplicada.
5. **Media — recursos inexistentes.** Consultas por username usan resultados potencialmente null; pueden terminar en 500. Las consultas por ID manejan EntityNotFoundException, pero no todos los caminos tienen una respuesta 404 consistente.
6. **Media — validaciones incompletas.** La actualización del email rechaza incluso el correo actual del propio usuario. Actualizaciones con nombre/apellido vacíos pueden fallar en substring. Las actualizaciones de tópicos no repiten la regla de duplicados.
7. **Media — borrado lógico y listados.** Listados de respuestas no filtran borrado; revisar qué información debe seguir visible.
8. **Media — reproducibilidad.** Faltan wrapper completo, migraciones y pruebas de integración CRUD/autorización. El único test original contextLoads necesita MySQL real. La compilación por caché local no demuestra que un equipo limpio resuelva todas las dependencias.

No se intentó actualizar versiones del framework ni se consultaron avisos externos de vulnerabilidades. Pendiente ejecutar Maven y pruebas HTTP con una base de desarrollo accesible, y revisar los hallazgos anteriores antes de producción.
