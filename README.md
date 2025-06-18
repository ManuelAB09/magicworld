# TFG-parqueaventura

Repositorio del proyecto TFG para la gestión de un parque de atracciones ficticio.  
Tecnologías principales: **Angular** (frontend) y **Spring Boot** (backend, Java), con base de datos **MySQL**.

## Estructura del proyecto

- **Backend:** Spring Boot (Java)
- **Frontend:** Angular
- **Base de datos:** MySQL
- **Migraciones:** Scripts SQL ejecutados con Maven

## Configuración inicial

1. Clona el repositorio: `git clone https://github.com/Manuelgithuv/TFG-parqueaventura.git`
2. Configura la base de datos MySQL y añade las credenciales como variables de entorno en `src/main/resources/application.properties`.  
   Asegúrate de que la base de datos existe y las credenciales son correctas.
3. Instala las dependencias del backend: `mvn clean install -DDB_USER=username -DDB_PASS=password`

## Ejecución de scripts SQL de migración

Para ejecutar los scripts SQL de migración, usa el siguiente comando:

```powershell
mvn clean sql:execute -DDB_USER=username -DDB_PASS=password
```
## Ejecución del backend: mvn spring-boot:run

## Contacto

Para dudas o sugerencias, contacta a través del siguiente correo: manartbel@alum.us.es