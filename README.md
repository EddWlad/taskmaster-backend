# TaskMaster – API de Gestión de Tareas

API REST desarrollada en **Java 21** con **Spring Boot 3.5**, siguiendo la prueba técnica de gestión de tareas.  
Incluye: usuarios, tareas, relación Usuario↔Tareas, validaciones, manejo de errores, **semillas iniciales**, pruebas unitarias y configuración opcional con **Docker Compose**.

---

**Video instructivo**:  
  Se incluye un video demostrativo donde se explica la ejecución del proyecto, el uso de los endpoints principales y la verificación de las semillas de usuarios/tareas, verificar el link para revision del video. https://drive.google.com/file/d/1KeNnszBxi71elMD1Lc4dNzDs-TH0oTkt/view?usp=sharing.   

## Requisitos

- Java 21
- Maven 3.9+
- Docker + Docker Compose (opcional para levantar la BD)
- PostgreSQL (si no usas Docker)

---

## Variables de entorno

El proyecto usa **dos archivos locales** (no se suben a Git):

### `.env` (para Docker Compose)

```dotenv
POSTGRES_USER=root
POSTGRES_PASSWORD=root
POSTGRES_DB=db_taskMaster

DB_LOCAL_PORT=5484
DB_DOCKER_PORT=5432
DB_VOLUME_URL=/var/lib/postgresql/data
```

### `.env.properties` (para secret de JWT)

```properties
# Secreto de JWT (no exponer en Git)
Se envia el .env.properties adjunto al correo de respuesta se debe copiar en la raiz

# Opcional: activar semillas en local
# app.seed.enabled=true
```

Y en `src/main/resources/application.properties`:

```properties
spring.config.import=optional:file:.env.properties
jwt.secret=${JWT_SECRET:dev-change-me}
```

---

## Semillas (DataSeeder)

Controladas por la propiedad:

```properties
app.seed.enabled=true
```

Semillas creadas:

- **Roles**: `ADMINISTRADOR`, `DESARROLLADOR`
- **Usuarios iniciales**:
  - `Ing.Robinson Ortega` – `desarrollo@abitmedia.cloud` – pass: `admin123`
  - `Ing.Edison Morocho` – `desarrollador1@abitmedia.cloud` – pass: `dev123`
- **Asignación de roles**: administrador ↔ ADMINISTRADOR, desarrollador ↔ DESARROLLADOR

> Puedes desactivar las semillas cambiando a `false` en `application.properties`.

---

## Cómo ejecutar

### Opción A — Solo backend (usando Postgres local o ya levantado)

```bash
./mvnw spring-boot:run
```

### Opción B — Con Docker Compose (recomendado)

1. Crea el archivo `.env` con las variables anteriores. (Paso no necesario puesto que ya se encuentra subido el .env solo verificarlo
2. Levanta la base de datos:

   ```bash
   docker compose up -d
   ```
Esto si es pr consola o directamente play desde cual IDE
3. Arranca el backend:

   ```bash
   ./mvnw spring-boot:run
   ```

> **Reiniciar en limpio** (borrar volumen con datos previos):  
> `docker compose down -v && docker compose up -d`

---

## Endpoints principales

### Usuarios

- `POST /users` → crear usuario con roles
- `GET /users/{id}`, `PUT /users/{id}`, `DELETE /users/{id}`
- `GET /users/admins`
- `GET /users/developers`

### Tareas

- `POST /task` → crear tarea
- `GET /task` → listar todas (según permisos)
- `GET /task/{id}` → detalle
- `GET /task/user/{idUser}` → listar por usuario
- `PUT /task/{id}` → actualizar
- `DELETE /task/{id}` → eliminación lógica

### Auth (plus)

- `POST /login` → autentica usuario y devuelve JWT

---

## Validaciones y reglas

- Emails únicos (`User.email`).
- Passwords encriptadas con **BCrypt**.
- Tareas solo accesibles por su **dueño** (o rol `ADMINISTRADOR`).
- Manejo global de errores (`ResponseExceptionHandler`).
- Uso de DTOs para transporte (`UserDTO`, `TaskDTO`, etc.).

---

## Pruebas unitarias

Ejecutar:

```bash
./mvnw test
```

Pruebas incluidas:

- **TaskServiceImplTest**:
  - Elimina una tarea como dueño (soft delete).
  - Rechaza eliminación si no es dueño (403).
- **UserServiceImplTest**:
  - Actualiza usuario re-encodeando contraseña.
  - Guarda roles limpiando previos y reasignando.

---

## Extras

- **JWT + Roles**: endpoints seguros con `@PreAuthorize`.
- **Docker Compose**: servicio de Postgres persistente.
- **Arquitectura limpia**: capas `controller`, `service`, `repository`, `entity`, `dto`, `exception`.

---

## Colección Postman

Se incluye la colección **TaskMaster-API** para probar los endpoints.  
Archivo: `TaskMaster-API.postman_collection.json`

Importar en Postman:

1. Abrir Postman → **Import**.
2. Seleccionar el archivo `.json` de la colección.
3. Ejecutar las requests: `User`, `Role`, `Task`, `Login`.

## Entregables solicitados

- ✅ API REST con endpoints claros
- ✅ Validaciones básicas
- ✅ Relación Usuario – Tareas
- ✅ Arquitectura limpia / SOLID
- ✅ DTOs
- ✅ Manejo de errores
- ✅ README.md
- ✅ Pruebas unitarias
- ✅ Colección Postman (se recomienda exportar y añadir al repo)
- ✅ Docker Compose opcional
- ✅ Plus: Login y JWT
