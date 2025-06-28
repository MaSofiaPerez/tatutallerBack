package com.tatutaller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class BookingRequest {
    @NotNull(message = "El ID de la clase es obligatorio")
    private Long classId;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate bookingDate;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime bookingTime;

    private String notes; // Notas adicionales del cliente

    // Constructors
    public BookingRequest() {
    }

    // Getters and Setters
    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
