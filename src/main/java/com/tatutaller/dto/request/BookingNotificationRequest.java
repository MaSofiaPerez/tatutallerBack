package com.tatutaller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;

public class BookingNotificationRequest {
    @NotNull(message = "El ID de la reserva es obligatorio")
    private Long bookingId;

    @NotBlank(message = "El email del profesor es obligatorio")
    @Email(message = "El email del profesor debe ser válido")
    private String teacherEmail;

    @NotBlank(message = "El nombre del profesor es obligatorio")
    private String teacherName;

    @NotBlank(message = "El nombre del estudiante es obligatorio")
    private String studentName;

    @NotBlank(message = "El email del estudiante es obligatorio")
    @Email(message = "El email del estudiante debe ser válido")
    private String studentEmail;

    @NotBlank(message = "El nombre de la clase es obligatorio")
    private String className;

    @NotBlank(message = "La fecha de la reserva es obligatoria")
    private String bookingDate;

    @NotBlank(message = "La hora de la reserva es obligatoria")
    private String bookingTime;

    private String notes; // Opcional

    // Constructors
    public BookingNotificationRequest() {
    }

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
