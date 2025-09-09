package com.tatutaller.service;

import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tatutaller.dto.request.BookingRequest;
import com.tatutaller.entity.Booking;
import com.tatutaller.entity.ClassEntity;
import com.tatutaller.entity.User;
import com.tatutaller.repository.BookingRepository;
import com.tatutaller.repository.ClassRepository;
import com.tatutaller.repository.UserRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Booking> createBooking(Long userId, BookingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        ClassEntity classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new NoSuchElementException("Clase no encontrada"));

        // Validar horario
        if (request.getStartTime().isBefore(classEntity.getStartTime()) ||
                request.getEndTime().isAfter(classEntity.getEndTime())) {
            throw new IllegalArgumentException("El horario de la reserva debe estar dentro del horario de la clase");
        }

        List<Booking> bookings = new ArrayList<>();

        if (Booking.BookingType.valueOf(request.getBookingType()) == Booking.BookingType.RECURRENTE) {
            // Crear exactamente 4 reservas semanales a partir de bookingDate
            LocalDate current = request.getBookingDate();
            for (int i = 0; i < 4; i++) {
                Booking booking = new Booking();
                booking.setUser(user);
                booking.setClassEntity(classEntity);
                booking.setBookingDate(current);
                booking.setStartTime(request.getStartTime());
                booking.setEndTime(request.getEndTime());
                booking.setType(Booking.BookingType.RECURRENTE);
                booking.setRecurrenceEndDate(current.plusWeeks(3)); // Opcional: marca la última fecha
                booking.setRecurrencePattern("WEEKLY");
                booking.setStatus(Booking.BookingStatus.PENDING);
                bookings.add(bookingRepository.save(booking));
                current = current.plusWeeks(1);
            }
        } else {
            // PUNTUAL
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setClassEntity(classEntity);
            booking.setBookingDate(request.getBookingDate());
            booking.setStartTime(request.getStartTime());
            booking.setEndTime(request.getEndTime());
            booking.setType(Booking.BookingType.PUNTUAL);
            booking.setStatus(Booking.BookingStatus.PENDING);
            bookings.add(bookingRepository.save(booking));
        }
        return bookings;
    }

    private boolean isSlotAvailable(ClassEntity classEntity, java.time.LocalDate date, java.time.LocalTime start,
            java.time.LocalTime end) {
        // Trae todas las reservas que se solapan con el slot pedido
        var overlapping = bookingRepository.findOverlappingBookings(classEntity, date, start, end);
        // Si hay 6 o más, no hay cupo
        return overlapping.size() < 6;
    }

}
