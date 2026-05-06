# TFG-magicworld

Repositorio del proyecto TFG para la gestión de un parque de atracciones ficticio.  
Tecnologías principales: **Angular** (frontend) y **Spring Boot** (backend, Java), con base de datos **MySQL**.

## Estructura del proyecto

- **Backend:** Spring Boot (Java 21)
- **Frontend:** Angular (v20)
- **Base de datos:** MySQL 8
- **Migraciones:** Scripts SQL ejecutados con Maven

---

## Manual de Instalación

Este manual describe cómo preparar el entorno de MagicWorld, configurar sus variables, levantar la aplicación y ejecutar la batería de pruebas.

### 1. Requisitos previos
Es indispensable instalar las siguientes herramientas y referenciarlas en la variable `PATH` de tu sistema:

- **Git:** Última versión estable.
- **Java (JDK 21):** Definir variable `JAVA_HOME`.
- **Apache Maven (3.9+):** Definir variable `MAVEN_HOME`.
- **Node.js (24.15.x)** y **npm (11.4.x)**.
- **Angular CLI:** Instalable mediante ` npm install-g @angular/cli@20.`.
- **Docker y Docker Compose:** (Opcional) Para ejecución mediante contenedores.
- **Allure CLI (2.x):** Para la visualización de los reportes de pruebas.

### 2. Preparación de la base de datos (MySQL 8)
Debes disponer de una instancia local de MySQL en el puerto `3306`. Ejecuta el siguiente script para crear la base de datos y el usuario gestor:

```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY 'tfg$root';

CREATE DATABASE IF NOT EXISTS magicworld CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'magicuser'@'localhost' IDENTIFIED BY 'RootPass123!';

GRANT ALL PRIVILEGES ON magicworld.* TO 'magicuser'@'localhost';
FLUSH PRIVILEGES;
```
*Nota: Si modificas estas credenciales, actualízalas también en el `pom.xml` (sql-maven-plugin).*

### 3. Configuración del entorno (.env)
Clona el repositorio y crea un archivo llamado `.env` en el **directorio raíz** del proyecto. Utiliza la siguiente plantilla (completa los valores vacíos con tus propias credenciales o usa los valores del manual de instalación de la memoria):

```bash
# Base de Datos
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/magicworld?connectionCollation=utf8mb4_unicode_ci
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=tfg$root
MYSQL_ROOT_PASSWORD=tfg$root
MYSQL_DATABASE=magicworld
MYSQL_USER=magicuser
MYSQL_PASSWORD=RootPass123!

# Seguridad y Autenticación
JWT_SECRET=TU_CLAVE_SECRETA_JWT_AQUI

# Configuración de Correo (SMTP)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_PROTOCOL=smtp
SPRING_MAIL_USERNAME=tu-correo@gmail.com
SPRING_MAIL_PASSWORD=tu-contraseña-de-aplicacion

# Integraciones de Terceros
FRONTEND_URL=http://localhost:4200
GEMINI_API_KEY=TU_API_KEY_DE_GEMINI
GOOGLE_CLIENT_ID=TU_GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET=TU_GOOGLE_CLIENT_SECRET
STRIPE_SECRET_KEY=TU_STRIPE_SECRET_KEY
STRIPE_PUBLIC_KEY=TU_STRIPE_PUBLIC_KEY
SENDGRID_API_KEY=TU_SENDGRID_API_KEY
```

### 4. Ejecución del sistema

#### Opción A: Ejecución en Local
**Backend:**
```bash
# En la raíz del proyecto
mvn clean install
mvn spring-boot:run
```
**Frontend:**
```bash
# En la carpeta /frontend
npm install
ng serve
```

#### Opción B: Ejecución Dockerizada
Asegúrate de que el puerto 3306 esté libre y Docker en ejecución.
```bash
# En Windows (PowerShell) desde docker/scripts
.\build-and-run.ps1

# En Linux (Bash) desde docker/scripts
chmod +x *.sh
./build-and-run.sh
```
*Acceso: Frontend en `http://localhost:4200` | API en `http://localhost:8080`*

---

## Calidad de Software (QA) y Reportes

Comandos para ejecutar las pruebas automatizadas:

**Backend:**
- Pruebas unitarias: `mvn clean test`
- Cobertura de mutación: `mvn org.pitest:pitest-maven:mutationCoverage`

**Frontend:**
- Unitarias: `ng test --coverage`
- Mutación: `npm run test:mutation`
- End-to-End (Cypress): `npm run cy:run` (o `npm run cy:open` para modo interactivo)

**Reportes Allure:**
```bash
allure serve allure-results
```

---

## Contacto

Para dudas o sugerencias, contacta a través del siguiente correo: [manartbel@alum.us.es](mailto:manartbel@alum.us.es)