package com.tatutaller.dto.response;

import java.time.LocalTime;

public class ClassResponse {
    private Long id;
    private String name;
    private String weekDay; // Ej: "Lunes"
    private String instructor; // Nombre del profesor
    private LocalTime startTime;
    private LocalTime endTime; // NUEVO: hora de fin
    private String duration; // String para mostrar "3 horas", etc.

    public ClassResponse(Long id, String name, String weekDay, String instructor, LocalTime startTime,
            LocalTime endTime, String duration) {
        this.id = id;
        this.name = name;
        this.weekDay = weekDay;
        this.instructor = instructor;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public String getInstructor() {
        return instructor;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
