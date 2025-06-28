<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# TatuTaller Backend - Copilot Instructions

Este es un proyecto Spring Boot para un taller de cerámica. Aquí están las instrucciones específicas para GitHub Copilot:

## Contexto del Proyecto

- **Dominio**: Taller de cerámica artesanal
- **Arquitectura**: Spring Boot + REST API + JWT Authentication
- **Base de datos**: H2 (desarrollo) / MySQL (producción)
- **Frontend**: React + Redux (repositorio separado)

## Convenciones de Código

### Entidades

- Usar anotaciones JPA estándar
- Incluir validaciones con `@Valid` y constraints
- Lifecycle methods para timestamps (`@PrePersist`, `@PreUpdate`)
- Enums para estados y categorías

### Controladores

- Usar `@CrossOrigin` para CORS
- Validar requests con `@Valid`
- Manejar errores con try-catch y responses apropiados
- Endpoints públicos en `/api/public/`
- Endpoints admin en `/api/admin/`
- Usar `@PreAuthorize` para seguridad

### Servicios

- Lógica de negocio separada de controladores
- Transacciones con `@Transactional` cuando sea necesario
- Manejo de excepciones personalizado

### Seguridad

- JWT para autenticación
- Roles: USER, ADMIN
- UserPrincipal personalizado
- Filtros de autenticación

## Estilo de Respuestas

- Entidades completas para operaciones CRUD
- Maps con mensajes para confirmaciones
- Estructura consistente de errores

## Funcionalidades Principales

1. **Autenticación**: Login/Register con JWT
2. **Gestión de Usuarios**: CRUD admin
3. **Productos**: Catálogo público + gestión admin
4. **Clases**: Listado público + gestión admin
5. **Reservas**: Sistema completo con estados
6. **Dashboard**: Estadísticas administrativas

Cuando generes código, mantén estas convenciones y el contexto del dominio del taller de cerámica.
