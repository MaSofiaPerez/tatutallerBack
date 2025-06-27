# TatuTaller Backend

Backend para el sistema de gestión de TatuTaller - Taller de Cerámica

## Descripción

Este es el backend desarrollado con Spring Boot que proporciona una API REST completa para la gestión de un taller de cerámica. Incluye funcionalidades para autenticación, gestión de usuarios, productos, clases y sistema de reservas.

## Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** - Autenticación y autorización con JWT
- **Spring Data JPA** - Persistencia de datos
- **H2 Database** - Base de datos en memoria para desarrollo
- **MySQL** - Base de datos para producción
- **Maven** - Gestión de dependencias
- **JWT** - Tokens de autenticación

## Características Principales

### Autenticación y Seguridad
- Sistema de autenticación con JWT
- Registro y login de usuarios
- Roles de usuario (USER, ADMIN)
- Protección de endpoints administrativos

### Gestión de Entidades
- **Usuarios**: CRUD completo con roles y estados
- **Productos**: Catálogo de productos de cerámica
- **Clases**: Gestión de clases y talleres
- **Reservas**: Sistema de reservas con estados

### Dashboard Administrativo
- Estadísticas del taller
- Métricas de usuarios, reservas e ingresos
- Gestión completa desde panel de administración

## Estructura del Proyecto

```
src/main/java/com/tatutaller/
├── config/                 # Configuraciones
│   ├── WebSecurityConfig.java
│   └── DataLoader.java
├── controller/             # Controladores REST
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ProductController.java
│   ├── ClassController.java
│   ├── BookingController.java
│   └── DashboardController.java
├── dto/                    # Data Transfer Objects
│   ├── request/
│   └── response/
├── entity/                 # Entidades JPA
│   ├── User.java
│   ├── Product.java
│   ├── ClassEntity.java
│   └── Booking.java
├── repository/             # Repositorios JPA
├── security/               # Configuración de seguridad
│   ├── JwtUtils.java
│   ├── UserPrincipal.java
│   └── AuthTokenFilter.java
└── service/                # Servicios de negocio
    ├── AuthService.java
    ├── DashboardService.java
    └── UserDetailsServiceImpl.java
```

## API Endpoints

### Autenticación
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registrar usuario

### Usuarios (Admin)
- `GET /api/admin/users` - Listar usuarios
- `GET /api/admin/users/{id}` - Obtener usuario
- `PUT /api/admin/users/{id}` - Actualizar usuario
- `DELETE /api/admin/users/{id}` - Eliminar usuario

### Productos
- `GET /api/public/products` - Listar productos (público)
- `GET /api/admin/products` - Listar productos (admin)
- `POST /api/admin/products` - Crear producto
- `PUT /api/admin/products/{id}` - Actualizar producto
- `DELETE /api/admin/products/{id}` - Eliminar producto

### Clases
- `GET /api/public/classes` - Listar clases (público)
- `GET /api/admin/classes` - Listar clases (admin)
- `POST /api/admin/classes` - Crear clase
- `PUT /api/admin/classes/{id}` - Actualizar clase
- `DELETE /api/admin/classes/{id}` - Eliminar clase

### Reservas
- `POST /api/bookings` - Crear reserva (usuario autenticado)
- `GET /api/my-bookings` - Mis reservas (usuario autenticado)
- `GET /api/admin/bookings` - Listar reservas (admin)
- `PUT /api/admin/bookings/{id}/status` - Actualizar estado (admin)
- `DELETE /api/admin/bookings/{id}` - Eliminar reserva (admin)

### Dashboard (Admin)
- `GET /api/admin/dashboard/stats` - Estadísticas
- `GET /api/admin/dashboard/recent-bookings` - Reservas recientes

## Configuración

### Requisitos Previos
- Java 17 o superior
- Maven 3.6 o superior
- MySQL 8.0 (para producción)

### Instalación y Ejecución

1. **Clonar el repositorio**
```bash
git clone [url-del-repositorio]
cd TatuTallerBACK
```

2. **Compilar el proyecto**
```bash
mvn clean install
```

3. **Ejecutar en modo desarrollo (H2)**
```bash
mvn spring-boot:run
```

4. **Configurar para producción (MySQL)**
   - Editar `src/main/resources/application.properties`
   - Descomentar las líneas de MySQL
   - Configurar credenciales de base de datos

### Variables de Configuración

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/tatutaller
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password

# JWT
jwt.secret=TuClaveSecretaMuySegura
jwt.expiration=86400000

# CORS
cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

## Datos de Prueba

El sistema carga automáticamente datos de prueba al iniciar:

### Usuarios
- **Admin**: admin@tatutaller.com / admin123
- **Usuario**: user@test.com / user123

### Productos
- Arcilla Blanca
- Esmalte Azul Cobalto
- Torno de Cerámica

### Clases
- Introducción a la Cerámica
- Técnicas de Esmaltado
- Torno Avanzado

## Desarrollo

### Agregar Nuevas Entidades
1. Crear entity en `entity/`
2. Crear repository en `repository/`
3. Crear controller en `controller/`
4. Agregar service si es necesario

### Testing
```bash
mvn test
```

### Base de Datos H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Usuario: sa
- Password: password

## Integración con Frontend

Este backend está diseñado para integrarse con el frontend de React que se encuentra en el repositorio TatuTallerFRONT.

### Headers Requeridos
```javascript
// Para requests autenticados
headers: {
  'Authorization': 'Bearer ' + token,
  'Content-Type': 'application/json'
}
```

### Formato de Respuestas
- **Login exitoso**: `{ token, type, id, name, email, role }`
- **Error**: `{ message: "Descripción del error" }`

## Licencia

Este proyecto es desarrollado como parte de un proyecto académico.

## Contacto

Para consultas sobre el proyecto, contactar al equipo de desarrollo.
