package com.tatutaller.controller;

import com.tatutaller.entity.Booking;
import com.tatutaller.entity.ClassEntity;
import com.tatutaller.entity.User;
import com.tatutaller.repository.BookingRepository;
import com.tatutaller.repository.ClassRepository;
import com.tatutaller.repository.UserRepository;
import com.tatutaller.security.UserPrincipal;
import com.tatutaller.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class TeacherController {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Obtener las clases del profesor actual
    @GetMapping("/my-classes")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<ClassEntity>> getMyClasses(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<ClassEntity> myClasses = classRepository.findByInstructor(userOpt.get());
            return ResponseEntity.ok(myClasses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener reservas de las clases del profesor
    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getMyClassBookings(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<ClassEntity> myClasses = classRepository.findByInstructor(userOpt.get());
            List<Booking> bookings = bookingRepository.findByClassEntityIn(myClasses);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Actualizar estado de una reserva (solo de sus clases)
    @PutMapping("/bookings/{id}/status")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(id);
            Map<String, String> response = new HashMap<>();
            
            if (bookingOpt.isEmpty()) {
                response.put("message", "Reserva no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Booking booking = bookingOpt.get();
            
            // Verificar que la reserva pertenece a una clase del profesor
            Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
            if (userOpt.isEmpty()) {
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User teacher = userOpt.get();
            if (!booking.getClassEntity().getInstructor().getId().equals(teacher.getId())) {
                response.put("message", "No tienes permisos para modificar esta reserva");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            String status = request.get("status");
            Booking.BookingStatus oldStatus = booking.getStatus();
            Booking.BookingStatus newStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
            
            booking.setStatus(newStatus);
            bookingRepository.save(booking);

            // Enviar email al estudiante según el nuevo estado
            try {
                if (newStatus == Booking.BookingStatus.CONFIRMED && oldStatus != Booking.BookingStatus.CONFIRMED) {
                    // Reserva confirmada
                    emailService.sendBookingConfirmationToStudent(
                        booking.getUser().getEmail(),
                        booking.getUser().getName(),
                        booking.getClassEntity().getName(),
                        teacher.getName(),
                        booking.getBookingDate().toString(),
                        booking.getBookingTime().toString()
                    );
                } else if (newStatus == Booking.BookingStatus.CANCELLED) {
                    // Reserva cancelada
                    String reason = request.get("reason");
                    emailService.sendBookingCancellationToStudent(
                        booking.getUser().getEmail(),
                        booking.getUser().getName(),
                        booking.getClassEntity().getName(),
                        reason
                    );
                }
            } catch (Exception e) {
                // Log error but don't fail the status update
                System.err.println("Error enviando email: " + e.getMessage());
            }

            response.put("message", "Estado de reserva actualizado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error al actualizar estado de reserva: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Eliminar alumno de una reserva (solo de sus clases)
    @DeleteMapping("/bookings/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(id);
            Map<String, String> response = new HashMap<>();
            
            if (bookingOpt.isEmpty()) {
                response.put("message", "Reserva no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Booking booking = bookingOpt.get();
            
            // Verificar que la reserva pertenece a una clase del profesor
            Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
            if (userOpt.isEmpty()) {
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User teacher = userOpt.get();
            if (!booking.getClassEntity().getInstructor().getId().equals(teacher.getId())) {
                response.put("message", "No tienes permisos para eliminar esta reserva");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            bookingRepository.delete(booking);

            response.put("message", "Alumno eliminado de la clase exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error al eliminar reserva: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Obtener detalles de alumnos en una clase específica
    @GetMapping("/classes/{id}/students")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getClassStudents(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Optional<ClassEntity> classOpt = classRepository.findById(id);
            if (classOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ClassEntity classEntity = classOpt.get();
            
            // Verificar que la clase pertenece al profesor
            Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User teacher = userOpt.get();
            if (!classEntity.getInstructor().getId().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Booking> students = bookingRepository.findByClassEntity(classEntity);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
