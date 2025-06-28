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

### GET /api/admin/users/teachers

**Headers:** `Authorization: Bearer <token>`  
**Obtener solo usuarios con rol de profesor**

### PUT /api/admin/users/{id}

**Headers:** `Authorization: Bearer <token>`

```json
{
  "name": "Usuario Actualizado",
  "email": "usuario@example.com",
  "phone": "+1234567890",
  "address": "Nueva dirección",
  "role": "teacher",
  "status": "activo"
}
```

**Valores válidos para role:** `"usuario"`, `"teacher"`, `"admin"`  
**Valores válidos para status:** `"activo"`, `"inactivo"`

### DELETE /api/admin/users/{id}

**Headers:** `Authorization: Bearer <token>`

### GET /api/users/{id}

**Headers:** `Authorization: Bearer <token>`  
**Endpoint privado para obtener datos completos de usuario**

**Respuesta exitosa:**

```json
{
  "id": 3,
  "name": "María Rodríguez",
  "email": "teacher1@tatutaller.com",
  "role": "TEACHER",
  "status": "ACTIVE",
  "phone": "+1234567890",
  "address": "Calle del Arte 123",
  "createdAt": "2025-06-27T14:38:06.675936",
  "updatedAt": "2025-06-27T14:38:06.675936"
}
```

**Diferencias con `/api/public/users/{id}`:**

- ✅ Requiere autenticación
- ✅ Incluye datos sensibles (teléfono, dirección, timestamps)
- ✅ Información completa del usuario

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

## 🔍 Usuarios Públicos

### GET /api/public/users/{id}

**Público - No requiere autenticación**  
**Obtener datos básicos de usuario por ID para la lógica escalonada del frontend**

**Ejemplo de uso:**

```javascript
// El frontend recibe una clase con solo instructorId:
{
  "id": 5,
  "name": "ceramica sofia",
  "instructorId": 3  // Solo el ID del profesor
}

// Automáticamente consulta datos del profesor:
const teacherData = await fetch(`/api/public/users/${instructorId}`);
```

**Respuesta exitosa:**

```json
{
  "id": 3,
  "name": "María Rodríguez",
  "email": "teacher1@tatutaller.com",
  "role": "teacher"
}
```

**Respuesta de error:**

```json
{
  "timestamp": "2025-06-27T21:17:30.302",
  "status": 404,
  "error": "Not Found",
  "path": "/api/public/users/999"
}
```

**🎯 Casos de uso:**

1. **Búsqueda escalonada del frontend**: Cuando la clase solo contiene `instructorId`
2. **Notificaciones por email**: Para obtener el email del profesor
3. **Datos básicos públicos**: Sin necesidad de autenticación
4. **Integración flexible**: Compatible con múltiples estructuras de backend

**🔧 Campos ID compatibles para profesores:**

- `instructorId`
- `instructor_id`
- `teacherId`
- `teacher_id`
- `userId`
- `user_id`

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

### GET /api/public/teachers

**Público - No requiere autenticación**  
**Obtener lista de profesores para combos de selección**

### GET /api/public/classes/{id}

**Público - No requiere autenticación**  
**Obtener detalles completos de una clase específica (incluyendo información del profesor)**

### GET /api/admin/classes

**Headers:** `Authorization: Bearer <token>`

### POST /api/admin/classes

**Headers:** `Authorization: Bearer <token>`

```json
{
  "name": "Nueva Clase",
  "description": "Descripción de la clase",
  "price": 80.0,
  "duration": "3 horas",
  "maxCapacity": 8,
  "level": "Principiante",
  "status": "Activo",
  "instructor": { "id": 3 },
  "materials": "Materiales incluidos",
  "requirements": "Requisitos"
}
```

**Valores válidos para level:** `"Principiante"`, `"Intermedio"`, `"Avanzado"`  
**Valores válidos para status:** `"Activo"`, `"Inactivo"`, `"Completo"`

### PUT /api/admin/classes/{id}

**Headers:** `Authorization: Bearer <token>`

### DELETE /api/admin/classes/{id}

**Headers:** `Authorization: Bearer <token>`

## 📅 Reservas

### POST /api/bookings

**Headers:** `Authorization: Bearer <token>`

```json
{
  "classId": 1,
  "bookingDate": "2025-07-15",
  "bookingTime": "10:00:00",
  "notes": "Observaciones del cliente"
}
```

**Nota:** El usuario se obtiene automáticamente del token JWT. No es necesario enviarlo en el cuerpo de la petición.

### POST /api/bookings/notify-teacher

**Headers:** `Authorization: Bearer <token>`  
**Enviar notificación por email al profesor sobre una nueva reserva**

```json
{
  "bookingId": 123,
  "teacherEmail": "profesor@tatutaller.com",
  "teacherName": "María García",
  "studentName": "Juan Pérez",
  "studentEmail": "juan@ejemplo.com",
  "className": "Clase de Torno",
  "bookingDate": "2025-01-15",
  "bookingTime": "14:00",
  "notes": "Primera vez en cerámica"
}
```

**Respuesta exitosa:**

```json
{
  "success": true,
  "message": "Email enviado exitosamente al profesor",
  "bookingId": 123,
  "teacherEmail": "profesor@tatutaller.com",
  "timestamp": "2025-06-27T20:30:15.123"
}
```

**Respuestas de error:**

```json
// Error 404 - Reserva no encontrada
{
  "success": false,
  "error": "Reserva no encontrada",
  "bookingId": 123
}

// Error 500 - Error del servidor
{
  "success": false,
  "error": "Error al enviar notificación por email",
  "details": "Connection timeout to SMTP server",
  "bookingId": 123,
  "timestamp": "2025-06-27T20:30:15.123"
}

// Error 400 - Datos inválidos (manejado por GlobalExceptionHandler)
{
  "error": "Validation failed",
  "details": {
    "teacherEmail": "El email del profesor debe ser válido",
    "bookingId": "El ID de la reserva es obligatorio"
  },
  "status": 400
}
```

**⚠️ Importante:**

- Este endpoint SIEMPRE devuelve JSON, nunca HTML o texto plano
- Usa `ResponseEntity<Map<String, Object>>` para garantizar tipo JSON
- Incluye `GlobalExceptionHandler` para errores de validación consistentes

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
const API_URL = "http://localhost:8080/api/auth";

// usersSlice.js
const API_URL = "http://localhost:8080/api/admin/users";

// teacherSlice.js (NUEVO)
const API_URL = "http://localhost:8080/api/teacher";

// productsSlice.js
const API_URL = "http://localhost:8080/api/admin/products";
const PUBLIC_API_URL = "http://localhost:8080/api/public/products";

// classesSlice.js
const API_URL = "http://localhost:8080/api/admin/classes";
const PUBLIC_API_URL = "http://localhost:8080/api/public/classes";

// bookingSlice.js
const API_URL = "http://localhost:8080/api/bookings";
const ADMIN_API_URL = "http://localhost:8080/api/admin/bookings";

// dashboardSlice.js
const API_URL = "http://localhost:8080/api/admin/dashboard";
```

## 📧 Flujo de Notificación por Email

### 1. Crear Reserva y Enviar Notificación (Frontend con Búsqueda Escalonada)

```javascript
// Paso 1: Crear la reserva
const bookingResponse = await fetch("/api/bookings", {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    classId: 1,
    bookingDate: "2025-07-15",
    bookingTime: "10:00:00",
    notes: "Primera vez",
  }),
});

const booking = await bookingResponse.json();

// Paso 2: Obtener detalles de la clase (puede incluir datos completos o solo ID)
const classResponse = await fetch(
  `/api/public/classes/${booking.classEntity.id}`
);
const classDetails = await classResponse.json();

// Paso 3: 🆕 BÚSQUEDA ESCALONADA para obtener email del profesor
let teacherEmail = null;
let teacherName = null;

// Opción A: Datos completos del instructor en la clase
if (classDetails.instructor && classDetails.instructor.email) {
  teacherEmail = classDetails.instructor.email;
  teacherName = classDetails.instructor.name;
  console.log("✅ Email del profesor obtenido de datos completos");
}
// Opción B: Usuario creador de la clase
else if (classDetails.user && classDetails.user.email) {
  teacherEmail = classDetails.user.email;
  teacherName = classDetails.user.name;
  console.log("✅ Email del profesor obtenido del usuario creador");
}
// Opción C: 🆕 NUEVO - Búsqueda por ID del instructor
else {
  // Buscar por diferentes nombres de campo posibles
  const instructorId =
    classDetails.instructorId ||
    classDetails.instructor_id ||
    classDetails.teacherId ||
    classDetails.teacher_id ||
    classDetails.userId ||
    classDetails.user_id;

  if (instructorId) {
    console.log(`🔍 Obteniendo datos del profesor con ID: ${instructorId}`);

    try {
      // Intentar endpoint privado primero
      const teacherResponse = await fetch(`/api/users/${instructorId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!teacherResponse.ok) {
        // Fallback al endpoint público
        const publicTeacherResponse = await fetch(
          `/api/public/users/${instructorId}`
        );
        const teacherData = await publicTeacherResponse.json();
        teacherEmail = teacherData.email;
        teacherName = teacherData.name;
        console.log("✅ Email del profesor obtenido desde endpoint público");
      } else {
        const teacherData = await teacherResponse.json();
        teacherEmail = teacherData.email;
        teacherName = teacherData.name;
        console.log("✅ Email del profesor obtenido desde endpoint privado");
      }
    } catch (error) {
      console.warn("⚠️ No se pudo obtener datos del profesor:", error);

      // 🔧 Fallback para desarrollo
      if (import.meta.env.DEV && import.meta.env.VITE_FALLBACK_TEACHER_EMAIL) {
        teacherEmail = import.meta.env.VITE_FALLBACK_TEACHER_EMAIL;
        teacherName = "Profesor (Desarrollo)";
        console.log("🔧 Usando email de fallback para desarrollo");
      }
    }
  }
}

// Paso 4: Enviar notificación al profesor
if (teacherEmail) {
  const notificationResponse = await fetch("/api/bookings/notify-teacher", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      bookingId: booking.id,
      teacherEmail: teacherEmail,
      teacherName: teacherName || "Profesor",
      studentName: userInfo.name,
      studentEmail: userInfo.email,
      className: classDetails.name,
      bookingDate: booking.bookingDate,
      bookingTime: booking.bookingTime,
      notes: booking.notes,
    }),
  });

  const notificationResult = await notificationResponse.json();
  console.log("📧 Email enviado:", notificationResult.success);
} else {
  console.error("❌ No se pudo obtener el email del profesor");
}
```

### 2. 🎯 Configuraciones de Backend Compatibles

El sistema frontend es **totalmente flexible** y funciona con cualquiera de estas estructuras:

**Opción A: Datos completos del instructor**

```json
{
  "id": 2,
  "name": "Técnicas de Esmaltado",
  "instructor": {
    "id": 4,
    "name": "Carlos Mendoza",
    "email": "teacher2@tatutaller.com"
  }
}
```

**Opción B: Solo ID del instructor (MÁS SIMPLE)**

```json
{
  "id": 5,
  "name": "ceramica sofia",
  "instructorId": 3 // Frontend automáticamente consulta GET /api/public/users/3
}
```

**Opción C: Diferentes nombres de campo**

```json
{
  "id": 5,
  "name": "ceramica sofia",
  "instructor_id": 3, // snake_case
  "teacherId": 3, // camelCase alternativo
  "teacher_id": 3, // snake_case alternativo
  "userId": 3, // si el creador es el profesor
  "user_id": 3 // snake_case para user
}
```

### 3. 🚀 Ventajas de la Nueva Implementación

- ✅ **Flexible**: Compatible con múltiples estructuras de backend
- ✅ **Eficiente**: Solo consulta usuario cuando es necesario
- ✅ **Fallback robusto**: Múltiples endpoints de respaldo
- ✅ **Debugging detallado**: Logs claros para identificar problemas
- ✅ **Desarrollo simplificado**: Email de fallback configurable
- ✅ **Sin dependencias**: Funciona inmediatamente sin cambios de backend

### 4. 📝 Variables de Entorno para Desarrollo

```env
# En tu archivo .env (para Vite)
VITE_FALLBACK_TEACHER_EMAIL=admin@tatutaller.com
```

El frontend detectará automáticamente si está en modo desarrollo y usará este email cuando no pueda obtener el del profesor.

## 🌐 CORS

El backend está configurado para aceptar requests desde:

- http://localhost:5173 (Vite)
- http://localhost:3000 (React dev server)

## 📋 Formato de Respuestas JSON

### ✅ Garantías del Backend:

1. **Todas las respuestas son JSON válido**

   - Nunca se devuelve HTML, XML o texto plano
   - Content-Type siempre es `application/json`

2. **Estructura consistente de errores:**

   ```json
   {
     "success": false,
     "error": "Descripción del error",
     "details": "Información adicional",
     "timestamp": "2025-06-27T20:30:15.123"
   }
   ```

3. **Estructura consistente de éxito:**

   ```json
   {
     "success": true,
     "message": "Operación completada",
     "data": {
       /* objeto de respuesta */
     },
     "timestamp": "2025-06-27T20:30:15.123"
   }
   ```

4. **GlobalExceptionHandler activo:**
   - Captura errores de validación Bean Validation
   - Convierte excepciones no manejadas a JSON
   - Mantiene consistencia en toda la API

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
