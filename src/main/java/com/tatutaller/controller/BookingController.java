package com.tatutaller.controller;

import com.tatutaller.entity.Booking;
import com.tatutaller.entity.User;
import com.tatutaller.entity.ClassEntity;
import com.tatutaller.repository.BookingRepository;
import com.tatutaller.repository.UserRepository;
import com.tatutaller.repository.ClassRepository;
import com.tatutaller.security.UserPrincipal;
import com.tatutaller.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class BookingController {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private EmailService emailService;

    // Endpoint para crear reserva (usuario autenticado)
    @PostMapping("/bookings")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createBooking(@Valid @RequestBody Booking booking, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Optional<User> user = userRepository.findByEmail(userPrincipal.getEmail());
            
            if (user.isPresent()) {
                booking.setUser(user.get());
                
                // Verificar que la clase existe
                Optional<ClassEntity> classEntity = classRepository.findById(booking.getClassEntity().getId());
                if (classEntity.isPresent()) {
                    booking.setClassEntity(classEntity.get());
                    
                    // Establecer estado inicial como PENDING
                    booking.setStatus(Booking.BookingStatus.PENDING);
                    
                    Booking savedBooking = bookingRepository.save(booking);
                    
                    // Enviar email al profesor
                    if (classEntity.get().getInstructor() != null) {
                        try {
                            emailService.sendBookingNotificationToTeacher(
                                classEntity.get().getInstructor().getEmail(),
                                classEntity.get().getInstructor().getName(),
                                user.get().getName(),
                                classEntity.get().getName(),
                                booking.getBookingDate().toString(),
                                booking.getBookingTime().toString()
                            );
                        } catch (Exception e) {
                            // Log error but don't fail the booking
                            System.err.println("Error enviando email al profesor: " + e.getMessage());
                        }
                    }
                    
                    return ResponseEntity.ok(savedBooking);
                } else {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Clase no encontrada");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error al crear reserva: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Endpoint para obtener reservas del usuario autenticado
    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getMyBookings(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> user = userRepository.findByEmail(userPrincipal.getEmail());
        
        if (user.isPresent()) {
            List<Booking> bookings = bookingRepository.findByUser(user.get());
            return ResponseEntity.ok(bookings);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Endpoints administrativos
    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/admin/bookings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        return booking.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/admin/bookings/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            String newStatus = statusUpdate.get("status");
            
            try {
                Booking.BookingStatus status = Booking.BookingStatus.valueOf(newStatus.toUpperCase());
                booking.setStatus(status);
                Booking updatedBooking = bookingRepository.save(booking);
                return ResponseEntity.ok(updatedBooking);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/admin/bookings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
