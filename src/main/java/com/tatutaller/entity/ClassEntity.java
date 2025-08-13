package com.tatutaller.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classes")
public class ClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la clase es obligatorio")
    @Column(length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "El día de la semana es obligatorio")
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(name = "start_time")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(length = 50)
    private String duration;

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    @Min(value = 1, message = "La capacidad máxima debe ser al menos 1")
    private Integer maxCapacity=1;

    @Enumerated(EnumType.STRING)
    private ClassLevel level;

    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "bookings" })
    private User instructor;

    private String materials; // Materiales incluidos
    private String requirements; // Requisitos previos

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({ "classEntity", "user" })
    private List<Booking> bookings = new ArrayList<>();

    public enum ClassLevel {
        BEGINNER("Principiante"),
        INTERMEDIATE("Intermedio"),
        ADVANCED("Avanzado"),
        ALL_LEVELS("Todos los Niveles");

        private final String displayName;

        ClassLevel(String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return displayName;
        }

        @JsonCreator
        public static ClassLevel fromDisplayName(String displayName) {
            for (ClassLevel level : ClassLevel.values()) {
                if (level.displayName.equalsIgnoreCase(displayName)) {
                    return level;
                }
            }
            // Fallback para valores en inglés
            try {
                return ClassLevel.valueOf(displayName.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Valor de nivel no válido: " + displayName);
            }
        }
    }

    public enum ClassStatus {
        ACTIVE("Activo"),
        INACTIVE("Inactivo"),
        FULL("Completo");

        private final String displayName;

        ClassStatus(String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return displayName;
        }

        @JsonCreator
        public static ClassStatus fromDisplayName(String displayName) {
            for (ClassStatus status : ClassStatus.values()) {
                if (status.displayName.equalsIgnoreCase(displayName)) {
                    return status;
                }
            }
            // Fallback para valores en inglés
            try {
                return ClassStatus.valueOf(displayName.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Valor de estado no válido: " + displayName);
            }
        }
    }

    // Constructors
    public ClassEntity() {
    }

    public ClassEntity(String name, BigDecimal price, DayOfWeek dayOfWeek,
            LocalTime startTime, LocalTime endTime, Integer maxCapacity, User instructor) {
        this.name = name;
        this.price = price;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = 1;
        this.instructor = instructor;
        this.status = ClassStatus.ACTIVE;
    }

    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDuration() {
        if (duration != null && !duration.trim().isEmpty()) {
            return duration;
        }
        if (startTime != null && endTime != null) {
            long hours = Duration.between(startTime, endTime).toHours();
            return hours + " horas";
        }
        return "Duración no definida";
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public ClassLevel getLevel() {
        return level;
    }

    public void setLevel(ClassLevel level) {
        this.level = level;
    }

    public ClassStatus getStatus() {
        return status;
    }

    public void setStatus(ClassStatus status) {
        this.status = status;
    }

    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    public String getMaterials() {
        return materials;
    }

    public void setMaterials(String materials) {
        this.materials = materials;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    private boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // Si no se han definido las horas, no hay validación
        }
        return startTime.isBefore(endTime);
    }
}
