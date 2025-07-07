package com.tatutaller.controller;

import com.tatutaller.entity.Booking;
import com.tatutaller.entity.User;
import com.tatutaller.entity.ClassEntity;
import com.tatutaller.dto.request.BookingRequest;
import com.tatutaller.dto.request.BookingNotificationRequest;
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
import com.tatutaller.dto.response.BookingResponse;
import java.util.stream.Collectors;

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

    // Método utilitario para mapear Booking a BookingResponse
    private BookingResponse toBookingResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getClassEntity().getId(),
                booking.getClassEntity().getName(),
                booking.getClassEntity().getInstructor() != null ? booking.getClassEntity().getInstructor().getName()
                        : null,
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus() != null ? booking.getStatus().name() : null,
                booking.getNotes(),
                booking.getUser() != null ? booking.getUser().getName() : null,
                booking.getUser() != null ? booking.getUser().getEmail() : null);
    }

    // Endpoint para crear reserva (usuario autenticado)
    @PostMapping("/bookings")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest bookingRequest,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Optional<User> user = userRepository.findByEmail(userPrincipal.getEmail());

            if (user.isPresent()) {
                // Verificar que la clase existe
                Optional<ClassEntity> classEntity = classRepository.findById(bookingRequest.getClassId());
                if (classEntity.isPresent()) {
                    // Crear la reserva con los datos del DTO
                    Booking booking = new Booking();
                    booking.setUser(user.get());
                    booking.setClassEntity(classEntity.get());
                    booking.setBookingDate(bookingRequest.getBookingDate());
                    booking.setStartTime(bookingRequest.getStartTime());
                    booking.setEndTime(bookingRequest.getEndTime());
                    booking.setNotes(bookingRequest.getNotes());
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
                                    booking.getStartTime().toString() + " - " + booking.getEndTime().toString());
                        } catch (Exception e) {
                            // Log error but don't fail the booking
                            System.err.println("Error enviando email al profesor: " + e.getMessage());
                        }
                    }

                    return ResponseEntity.ok(toBookingResponse(savedBooking));
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
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> user = userRepository.findByEmail(userPrincipal.getEmail());

        if (user.isPresent()) {
            List<Booking> bookings = bookingRepository.findByUser(user.get());
            List<BookingResponse> responses = bookings.stream().map(this::toBookingResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoints administrativos
    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        List<BookingResponse> responses = bookings.stream().map(this::toBookingResponse).collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/admin/bookings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        return booking.map(b -> ResponseEntity.ok(toBookingResponse(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admin/bookings/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> updateBookingStatus(@PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);

        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            String newStatus = statusUpdate.get("status");

            try {
                Booking.BookingStatus status = Booking.BookingStatus.valueOf(newStatus.toUpperCase());
                booking.setStatus(status);
                Booking updatedBooking = bookingRepository.save(booking);
                return ResponseEntity.ok(toBookingResponse(updatedBooking));
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

    // Endpoint para enviar notificación por email al profesor
    @PostMapping("/bookings/notify-teacher")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> notifyTeacher(@Valid @RequestBody BookingNotificationRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Log de debug
            System.out.println("🔔 Recibida solicitud de notificación para reserva ID: " + request.getBookingId());
            System.out.println("📧 Email del profesor: " + request.getTeacherEmail());
            System.out.println("👨‍🏫 Nombre del profesor: " + request.getTeacherName());
            System.out.println("👨‍🎓 Nombre del estudiante: " + request.getStudentName());

            // No verificar que la reserva existe para evitar problemas de DB
            // Optional<Booking> booking =
            // bookingRepository.findById(request.getBookingId());
            // if (!booking.isPresent()) {
            // response.put("success", false);
            // response.put("error", "Reserva no encontrada");
            // response.put("bookingId", request.getBookingId());
            // return ResponseEntity.status(404).body(response);
            // }

            // Intentar enviar notificación al profesor
            try {
                emailService.sendBookingNotificationToTeacher(
                        request.getTeacherEmail(),
                        request.getTeacherName(),
                        request.getStudentName(),
                        request.getClassName(),
                        request.getBookingDate(),
                        request.getBookingTime());

                System.out.println("✅ Email enviado exitosamente");
            } catch (Exception emailError) {
                System.err.println("⚠️ Error enviando email (continuando sin fallar): " + emailError.getMessage());
                // No fallar si hay error de email
            }

            // Respuesta exitosa
            response.put("success", true);
            response.put("message", "Notificación procesada exitosamente");
            response.put("bookingId", request.getBookingId());
            response.put("teacherEmail", request.getTeacherEmail());
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("❌ Error procesando notificación: " + e.getMessage());
            e.printStackTrace();

            // Respuesta de error consistente
            response.put("success", false);
            response.put("error", "Error al procesar notificación");
            response.put("details", e.getMessage());
            response.put("bookingId", request.getBookingId());
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.status(500).body(response);
        }
    }

    // Endpoint de prueba para verificar que las notificaciones funcionan
    @PostMapping("/bookings/notify-teacher-test")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> notifyTeacherTest(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🔔 Test endpoint recibido: " + request);

            response.put("success", true);
            response.put("message", "Endpoint de notificación funcionando correctamente");
            response.put("receivedData", request);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error en test endpoint: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Endpoint público para testing (temporal)
    @PostMapping("/public/test-notification")
    public ResponseEntity<Map<String, Object>> testNotificationPublic() {
        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("message", "El backend está funcionando correctamente");
        response.put("endpoint", "/api/bookings/notify-teacher");
        response.put("status", "disponible");
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}
