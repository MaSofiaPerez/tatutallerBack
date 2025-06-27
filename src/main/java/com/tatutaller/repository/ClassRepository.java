package com.tatutaller.repository;

import com.tatutaller.entity.ClassEntity;
import com.tatutaller.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
    
    List<ClassEntity> findByStatus(ClassEntity.ClassStatus status);
    
    List<ClassEntity> findByLevel(ClassEntity.ClassLevel level);
    
    List<ClassEntity> findByNameContainingIgnoreCase(String name);
    
    // Buscar clases por instructor
    List<ClassEntity> findByInstructor(User instructor);
    
    List<ClassEntity> findByInstructorAndStatus(User instructor, ClassEntity.ClassStatus status);
    
    @Query("SELECT c FROM ClassEntity c WHERE c.status = 'ACTIVE'")
    List<ClassEntity> findActiveClasses();
    
    @Query("SELECT COUNT(c) FROM ClassEntity c WHERE c.status = 'ACTIVE'")
    long countActiveClasses();
    
    @Query("SELECT c, COUNT(b) as bookingCount FROM ClassEntity c LEFT JOIN c.bookings b GROUP BY c ORDER BY bookingCount DESC")
    List<Object[]> findPopularClasses();
}
