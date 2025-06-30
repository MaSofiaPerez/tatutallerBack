package com.tatutaller.controller;

import com.tatutaller.entity.ClassEntity;
import com.tatutaller.entity.User;
import com.tatutaller.repository.ClassRepository;
import com.tatutaller.repository.UserRepository;
import com.tatutaller.service.UserService;
import com.tatutaller.dto.request.CreateClassRequest;
import com.tatutaller.dto.response.PublicClassResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class ClassController {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // Endpoint público para obtener clases
    @GetMapping("/public/classes")
    public ResponseEntity<List<PublicClassResponse>> getAllClasses() {
        List<ClassEntity> classes = classRepository.findActiveClasses();
        List<PublicClassResponse> publicClasses = classes.stream()
                .map(this::convertToPublicResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(publicClasses);
    }

    // Endpoint público para obtener profesores (para el combo de selección)
    @GetMapping("/public/teachers")
    public ResponseEntity<List<User>> getPublicTeachers() {
        List<User> teachers = classRepository.findAll().stream()
                .map(ClassEntity::getInstructor)
                .filter(instructor -> instructor != null && instructor.getRole() == User.Role.TEACHER)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(teachers);
    }

    private PublicClassResponse convertToPublicResponse(ClassEntity classEntity) {
        return new PublicClassResponse(
                classEntity.getId(),
                classEntity.getName(),
                classEntity.getDescription(),
                classEntity.getPrice(),
                classEntity.getDuration(),
                classEntity.getMaxCapacity(),
                classEntity.getLevel() != null ? classEntity.getLevel().toString() : null,
                classEntity.getStatus() != null ? classEntity.getStatus().toString() : null,
                classEntity.getInstructor() != null ? classEntity.getInstructor().getName() : null,
                classEntity.getMaterials(),
                classEntity.getRequirements());
    }

    // Endpoints administrativos
    @GetMapping("/admin/classes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClassEntity>> getAllClassesAdmin() {
        List<ClassEntity> classes = classRepository.findAll();
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/admin/classes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassEntity> getClassById(@PathVariable Long id) {
        Optional<ClassEntity> classEntity = classRepository.findById(id);
        return classEntity.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/classes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createClass(@Valid @RequestBody CreateClassRequest request) {
        try {
            User teacher = userService.findById(request.getTeacherId());

            if (teacher.getRole() != User.Role.TEACHER) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "El usuario seleccionado no es un profesor"));
            }

            ClassEntity classEntity = new ClassEntity(
                    request.getName(),
                    request.getPrice(),
                    request.getDayOfWeek(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getMaxCapacity(),
                    teacher);

            classEntity.setDescription(request.getDescription());
            classEntity.setDuration(request.getDuration());
            classEntity.setLevel(request.getLevel());
            classEntity.setMaterials(request.getMaterials());
            classEntity.setRequirements(request.getRequirements());

            ClassEntity savedClass = classRepository.save(classEntity);
            return ResponseEntity.ok(savedClass);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Error al crear la clase: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Error interno del servidor"));
        }
    }

    @PutMapping("/admin/classes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateClass(@PathVariable Long id,
            @Valid @RequestBody CreateClassRequest request) {
        try {
            // 1. Buscar clase existente
            Optional<ClassEntity> optionalClass = classRepository.findById(id);
            if (optionalClass.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 2. Buscar y validar el tallerista
            User teacher = userService.findById(request.getTeacherId());
            if (teacher.getRole() != User.Role.TEACHER) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "El usuario seleccionado no es un profesor"));
            }

            // 3. Mapear campos del request a Classentity
            ClassEntity classEntity = optionalClass.get();
            classEntity.setName(request.getName());
            classEntity.setDescription(request.getDescription());
            classEntity.setPrice(request.getPrice());
            classEntity.setDuration(request.getDuration());
            classEntity.setDayOfWeek(request.getDayOfWeek());
            classEntity.setStartTime(request.getStartTime());
            classEntity.setEndTime(request.getEndTime());
            classEntity.setMaxCapacity(request.getMaxCapacity());
            classEntity.setLevel(request.getLevel());
            classEntity.setMaterials(request.getMaterials());
            classEntity.setRequirements(request.getRequirements());
            classEntity.setInstructor(teacher);

            // 4. Guardar cambios
            ClassEntity updatedClass = classRepository.save(classEntity);
            return ResponseEntity.ok(updatedClass);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Error al actualizar la clase: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Error interno del servidor"));
        }
    }

    @DeleteMapping("/admin/classes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteClass(@PathVariable Long id) {
        if (classRepository.existsById(id)) {
            classRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint público para obtener detalles completos de una clase (incluyendo
    // profesor)
    @GetMapping("/public/classes/{id}")
    public ResponseEntity<ClassEntity> getClassDetails(@PathVariable Long id) {
        Optional<ClassEntity> classEntity = classRepository.findById(id);
        return classEntity.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint público para obtener datos básicos de un usuario por ID
    @GetMapping("/public/users/{id}")
    public ResponseEntity<Map<String, Object>> getPublicUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            User userData = user.get();
            Map<String, Object> publicUserData = new HashMap<>();
            publicUserData.put("id", userData.getId());
            publicUserData.put("name", userData.getName());
            publicUserData.put("email", userData.getEmail());
            publicUserData.put("role", userData.getRole().getDisplayName());

            return ResponseEntity.ok(publicUserData);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
