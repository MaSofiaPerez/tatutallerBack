package com.tatutaller.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingResponse {
    private Long id;
    private Long classId;
    private String className;
    private String teacherName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String notes;
    private int maxCapacity;
    private String userName;
    private String userEmail;

    public BookingResponse(Long id, Long classId, String className, String teacherName, LocalDate bookingDate,
            LocalTime startTime, LocalTime endTime, String status, String notes, int maxCapacity, String userName,
            String userEmail) {
        this.id = id;
        this.classId = classId;
        this.className = className;
        this.teacherName = teacherName;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.notes = notes;
        this.maxCapacity = 1;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public BookingResponse(Long id, String userName, LocalDate bookingDate, String status) {
        this.id = id;
        this.userName = userName;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
