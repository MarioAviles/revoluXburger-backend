# 📘 Documentación del Backend - Proyecto Spring Boot

Este documento proporciona información relevante para desarrolladores que trabajen con este backend desarrollado con Spring Boot. Incluye las rutas de acceso a la documentación Swagger, la consola de la base de datos H2, y detalles sobre dónde se encuentran las credenciales de configuración.

---

## 🚀 Rutas Principales

### 📄 Swagger - Documentación de la API
- **URL:** [http://localhost:8080/swagger-ui.html]
- **Descripción:** Interfaz gráfica proporcionada por Swagger para explorar y probar los distintos endpoints REST del proyecto.

### 🗄️ H2 Console - Consola de Base de Datos en memoria
- **URL:** [http://localhost:8080/h2-console]
- **Descripción:** Herramienta web para acceder a la base de datos H2 utilizada por la aplicación para pruebas. Permite ejecutar consultas SQL y verificar el estado de las tablas. Configuración comentada por defecto

### 🗄️ PostgreSQL - Consola de Base de Datos
- **Descripción:** Base de datos utilizada por la aplicación. Configuración puesta por defecto
---

## ⚙️ Configuración y Credenciales

Los parámetros de configuración, incluyendo las credenciales de acceso a la base de datos, se encuentran en el archivo src/main/resources/application.properties
