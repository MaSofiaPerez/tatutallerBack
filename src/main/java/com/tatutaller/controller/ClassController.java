package com.tatutaller.controller;

import com.tatutaller.entity.ClassEntity;
import com.tatutaller.repository.ClassRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class ClassController {
    
    @Autowired
    private ClassRepository classRepository;
    
    // Endpoint público para obtener clases
    @GetMapping("/public/classes")
    public ResponseEntity<List<ClassEntity>> getAllClasses() {
        List<ClassEntity> classes = classRepository.findActiveClasses();
        return ResponseEntity.ok(classes);
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
    public ResponseEntity<ClassEntity> createClass(@Valid @RequestBody ClassEntity classEntity) {
        ClassEntity savedClass = classRepository.save(classEntity);
        return ResponseEntity.ok(savedClass);
    }
    
    @PutMapping("/admin/classes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassEntity> updateClass(@PathVariable Long id, @Valid @RequestBody ClassEntity classDetails) {
        Optional<ClassEntity> optionalClass = classRepository.findById(id);
        
        if (optionalClass.isPresent()) {
            ClassEntity classEntity = optionalClass.get();
            classEntity.setName(classDetails.getName());
            classEntity.setDescription(classDetails.getDescription());
            classEntity.setPrice(classDetails.getPrice());
            classEntity.setDuration(classDetails.getDuration());
            classEntity.setMaxCapacity(classDetails.getMaxCapacity());
            classEntity.setLevel(classDetails.getLevel());
            classEntity.setStatus(classDetails.getStatus());
            classEntity.setInstructor(classDetails.getInstructor());
            classEntity.setMaterials(classDetails.getMaterials());
            classEntity.setRequirements(classDetails.getRequirements());
            
            ClassEntity updatedClass = classRepository.save(classEntity);
            return ResponseEntity.ok(updatedClass);
        } else {
            return ResponseEntity.notFound().build();
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
}
