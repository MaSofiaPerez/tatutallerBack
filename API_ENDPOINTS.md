# API Endpoints - TatuTaller Backend

El backend está corriendo en: **http://localhost:8080**

## 🔐 Autenticación

### POST /api/auth/login
**Login de usuario**
```json
{
  "email": "admin@tatutaller.com",
  "password": "admin123"
}
```

### POST /api/auth/register
**Registro de usuario**
```json
{
  "name": "Nuevo Usuario",
  "email": "nuevo@test.com",
  "password": "password123",
  "phone": "+1234567890",
  "address": "Dirección ejemplo"
}
```

## 👥 Usuarios (Solo Admin)

### GET /api/admin/users
**Headers:** `Authorization: Bearer <token>`

### PUT /api/admin/users/{id}
**Headers:** `Authorization: Bearer <token>`

### DELETE /api/admin/users/{id}
**Headers:** `Authorization: Bearer <token>`

## 👨‍🏫 Profesores

### GET /api/teacher/my-classes
**Headers:** `Authorization: Bearer <token>`
**Obtener las clases asignadas al profesor**

### GET /api/teacher/my-bookings
**Headers:** `Authorization: Bearer <token>`
**Obtener todas las reservas de las clases del profesor**

### GET /api/teacher/classes/{id}/students
**Headers:** `Authorization: Bearer <token>`
**Obtener estudiantes de una clase específica**

### PUT /api/teacher/bookings/{id}/status
**Headers:** `Authorization: Bearer <token>`
**Actualizar estado de una reserva (solo de sus clases)**
```json
{
  "status": "CONFIRMED"
}
```

### DELETE /api/teacher/bookings/{id}
**Headers:** `Authorization: Bearer <token>`
**Eliminar alumno de una reserva (solo de sus clases)**

## 🏺 Productos

### GET /api/public/products
**Público - No requiere autenticación**

### GET /api/admin/products
**Headers:** `Authorization: Bearer <token>`

### POST /api/admin/products
**Headers:** `Authorization: Bearer <token>`
```json
{
  "name": "Nuevo Producto",
  "description": "Descripción del producto",
  "price": 29.99,
  "stock": 10,
  "category": "MATERIALES",
  "status": "ACTIVE"
}
```

### PUT /api/admin/products/{id}
**Headers:** `Authorization: Bearer <token>`

### DELETE /api/admin/products/{id}
**Headers:** `Authorization: Bearer <token>`

## 🎨 Clases

### GET /api/public/classes
**Público - No requiere autenticación**

### GET /api/admin/classes
**Headers:** `Authorization: Bearer <token>`

### POST /api/admin/classes
**Headers:** `Authorization: Bearer <token>`
```json
{
  "name": "Nueva Clase",
  "description": "Descripción de la clase",
  "price": 80.00,
  "duration": "3 horas",
  "maxCapacity": 8,
  "level": "BEGINNER",
  "status": "ACTIVE",
  "instructor": {"id": 3},
  "materials": "Materiales incluidos",
  "requirements": "Requisitos"
}
```

### PUT /api/admin/classes/{id}
**Headers:** `Authorization: Bearer <token>`

### DELETE /api/admin/classes/{id}
**Headers:** `Authorization: Bearer <token>`

## 📅 Reservas

### POST /api/bookings
**Headers:** `Authorization: Bearer <token>`
```json
{
  "classEntity": {"id": 1},
  "bookingDate": "2025-07-15",
  "bookingTime": "10:00:00",
  "notes": "Observaciones del cliente"
}
```

### GET /api/my-bookings
**Headers:** `Authorization: Bearer <token>`

### GET /api/admin/bookings
**Headers:** `Authorization: Bearer <token>`

### PUT /api/admin/bookings/{id}/status
**Headers:** `Authorization: Bearer <token>`
```json
{
  "status": "CONFIRMED"
}
```

### DELETE /api/admin/bookings/{id}
**Headers:** `Authorization: Bearer <token>`

## 📊 Dashboard (Solo Admin)

### GET /api/admin/dashboard/stats
**Headers:** `Authorization: Bearer <token>`

### GET /api/admin/dashboard/recent-bookings
**Headers:** `Authorization: Bearer <token>`

## 🧪 Datos de Prueba

### Usuarios:
- **Admin**: admin@tatutaller.com / admin123
- **Usuario**: user@test.com / user123
- **Profesor 1**: teacher1@tatutaller.com / teacher123 (María Rodríguez)
- **Profesor 2**: teacher2@tatutaller.com / teacher123 (Carlos Mendoza)

### Productos:
- Arcilla Blanca ($25.00)
- Esmalte Azul Cobalto ($35.00)
- Torno de Cerámica ($1200.00)

### Clases:
- Introducción a la Cerámica ($80.00) - Instructor: María Rodríguez
- Técnicas de Esmaltado ($120.00) - Instructor: Carlos Mendoza
- Torno Avanzado ($150.00) - Instructor: María Rodríguez

## 🔗 URLs para el Frontend

Actualizar en los slices de Redux:

```javascript
// authSlice.js
const API_URL = 'http://localhost:8080/api/auth';

// usersSlice.js
const API_URL = 'http://localhost:8080/api/admin/users';

// teacherSlice.js (NUEVO)
const API_URL = 'http://localhost:8080/api/teacher';

// productsSlice.js
const API_URL = 'http://localhost:8080/api/admin/products';
const PUBLIC_API_URL = 'http://localhost:8080/api/public/products';

// classesSlice.js
const API_URL = 'http://localhost:8080/api/admin/classes';
const PUBLIC_API_URL = 'http://localhost:8080/api/public/classes';

// bookingSlice.js
const API_URL = 'http://localhost:8080/api/bookings';
const ADMIN_API_URL = 'http://localhost:8080/api/admin/bookings';

// dashboardSlice.js
const API_URL = 'http://localhost:8080/api/admin/dashboard';
```

## 🌐 CORS

El backend está configurado para aceptar requests desde:
- http://localhost:5173 (Vite)
- http://localhost:3000 (React dev server)

## 🗄️ Base de Datos H2

**Console**: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuario: `sa`
- Password: `password`

## 🔐 Roles de Usuario

### USER
- Realizar reservas
- Ver sus propias reservas
- Ver productos y clases públicas

### TEACHER
- Ver sus clases asignadas
- Ver reservas de sus clases
- Modificar estado de reservas de sus clases
- Eliminar alumnos de sus clases
- Ver detalles de estudiantes en sus clases

### ADMIN
- Acceso completo a todos los endpoints
- Gestión de usuarios, productos, clases y reservas
- Ver dashboard con estadísticas
