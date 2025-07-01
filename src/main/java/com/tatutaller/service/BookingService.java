package com.tatutaller.service;

import java.util.NoSuchElementException;

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

    public Booking createBooking(Long userId, BookingRequest request) {
        // Validar que el usuario exista
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        // Validar que la clase exista
        ClassEntity classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new NoSuchElementException("Clase no encontrada"));

        // Validar que el horario esté dentro del horario de clase
        if (request.getStartTime().isBefore(classEntity.getStartTime()) ||
                request.getEndTime().isAfter(classEntity.getEndTime())) {
            throw new IllegalArgumentException("El horario de la reserva debe estar dentro del horario de la clase");
        }

        // Si la reserva es RECURRENTE
        if (Booking.BookingType.valueOf(request.getBookingType()) == Booking.BookingType.RECURRENTE) {
            java.time.LocalDate current = request.getBookingDate();
            java.time.LocalDate end = request.getRecurrenceEndDate();
            java.util.List<Booking> bookings = new java.util.ArrayList<>();
            while (!current.isAfter(end)) {
                if (!isSlotAvailable(classEntity, current, request.getStartTime(), request.getEndTime())) {
                    throw new IllegalArgumentException("No hay cupo disponible para la clase en la fecha " + current);
                }
                Booking booking = new Booking();
                booking.setUser(user);
                booking.setClassEntity(classEntity);
                booking.setBookingDate(current);
                booking.setStartTime(request.getStartTime());
                booking.setEndTime(request.getEndTime());
                booking.setType(Booking.BookingType.RECURRENTE);
                booking.setNotes(request.getNotes());
                booking.setStatus(Booking.BookingStatus.PENDING);
                bookings.add(booking);
                current = current.plusWeeks(1);
            }
            bookingRepository.saveAll(bookings);
            return bookings.get(0); // O puedes devolver la lista si prefieres
        }

        // Validar cupo y solapamiento para reserva puntual
        if (!isSlotAvailable(classEntity, request.getBookingDate(), request.getStartTime(), request.getEndTime())) {
            throw new IllegalArgumentException("No hay cupo disponible para la clase en el horario solicitado");
        }

        // Crear la reserva puntual
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setClassEntity(classEntity);
        booking.setBookingDate(request.getBookingDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setType(Booking.BookingType.valueOf(request.getBookingType()));
        booking.setNotes(request.getNotes());
        booking.setStatus(Booking.BookingStatus.PENDING);

        // Guardar la reserva
        return bookingRepository.save(booking);
    }

    private boolean isSlotAvailable(ClassEntity classEntity, java.time.LocalDate date, java.time.LocalTime start,
            java.time.LocalTime end) {
        // Trae todas las reservas que se solapan con el slot pedido
        var overlapping = bookingRepository.findOverlappingBookings(classEntity, date, start, end);
        // Si hay 6 o más, no hay cupo
        return overlapping.size() < 6;
    }

}
