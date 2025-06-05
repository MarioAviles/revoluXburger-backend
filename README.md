
# 🍔 revoluXburger Backend

Backend del proyecto **revoluXburger**, una hamburguesería ficticia desarrollada como Trabajo de Fin de Grado. Esta API gestiona autenticación, usuarios, roles, reservas, subida de imágenes y recuperación de contraseña.

Desarrollado en **Java Spring Boot** con base de datos **PostgreSQL** y subida de imágenes a través de **Supabase**.

---

## 🚀 Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Security + JWT
- PostgreSQL / H2
- Supabase (opcional)
- Spring Mail
- Swagger OpenAPI
- Maven

---

## 📦 Clonar o descargar

```bash
git clone https://github.com/MarioAviles/revoluXburger-backend.git
cd revoluXburger-backend
```

O descarga el ZIP desde GitHub y descomprímelo.

---

## ⚙️ Configuración del entorno

### 1. Copiar archivo de configuración

Copia el archivo de ejemplo:

```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

Y edítalo según lo que quieras probar.

---

### 2. Elegir base de datos

#### ✅ Opción por defecto (recomendada para probar rápido): H2 en memoria

- Ya viene activada en el archivo de ejemplo.
- No requiere instalación.
- Se accede por navegador.

#### 🔄 Opción alternativa: PostgreSQL local

- Descomenta la sección correspondiente en el archivo `application.properties`.
- Asegúrate de tener PostgreSQL instalado y crea una base de datos llamada `revoluxburger`.

---

### 3. Supabase (opcional)

Para probar la **subida de imágenes**, crea un proyecto en [https://supabase.com](https://supabase.com) y añade:

- `SUPABASE_URL`
- `SUPABASE_BUCKET`
- `SUPABASE_API_KEY`

---

### 4. Email (opcional)

Para probar la **recuperación de contraseña**, configura:

- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`

Puedes usar Gmail u otro proveedor SMTP.

---

### 5. Ejecutar el proyecto

```bash
./mvnw spring-boot:run
```

O desde tu IDE (IntelliJ, Eclipse, etc).

Por defecto se ejecuta en:

```
http://localhost:8080
```

---

## 📚 Endpoints y herramientas disponibles

### 📖 Swagger (documentación y prueba de endpoints)

```
http://localhost:8080/swagger-ui/index.html
```

Desde ahí puedes probar login, registro, reservas, etc.

---

### 🧠 Consola H2 (si estás usando base de datos en memoria)

```
http://localhost:8080/h2-console
```

Usa estos datos para acceder:

- **URL JDBC:** `jdbc:h2:mem:testdb`
- **Usuario:** `user`
- **Contraseña:** `pass`

---

## 🔐 Rutas principales de la API

| Método | Ruta                       | Descripción                                 | Rol requerido    |
|--------|----------------------------|---------------------------------------------|------------------|
| POST   | `/auth/register`           | Registro de usuario                         | Público          |
| POST   | `/auth/login`              | Inicio de sesión y obtención de token JWT   | Público          |
| GET    | `/users/me`                | Obtener información del usuario actual      | Usuario o Admin  |
| GET    | `/users/all`               | Listar todos los usuarios                   | Admin            |
| POST   | `/reservations`            | Crear una reserva                           | Usuario          |
| GET    | `/reservations`            | Ver tus reservas                            | Usuario          |
| GET    | `/reservations/all`        | Ver todas las reservas                      | Admin            |
| POST   | `/upload`                  | Subir imagen a Supabase                     | Usuario/Admin    |
| POST   | `/auth/reset-password`     | Iniciar recuperación de contraseña por email| Público          |

> Más rutas disponibles en Swagger.

---

## 📁 Estructura del proyecto

```plaintext
src/
├── main/
│   ├── java/com/revoluxburger/  ← Código fuente
│   └── resources/
│       ├── application.properties
│       └── application-example.properties
```

---

## 👨‍🔬 Roles disponibles

- **USER**: puede registrarse, hacer login, ver su perfil y crear reservas.
- **ADMIN**: puede acceder a todos los usuarios, ver todas las reservas, gestionar imágenes, etc.

---

## ✅ Estado del proyecto

- [x] Login y registro con JWT
- [x] Gestión de roles
- [x] CRUD de reservas
- [x] Subida de imágenes (Supabase)
- [x] Recuperación de contraseña (email)
- [x] Documentación Swagger
- [x] Preparado para ejecución en local

---

> Si tienes cualquier problema para ejecutar el proyecto localmente, por favor revisa que hayas renombrado correctamente `application-example.properties` como `application.properties` y que tengas Java 21 y Maven instalados.
