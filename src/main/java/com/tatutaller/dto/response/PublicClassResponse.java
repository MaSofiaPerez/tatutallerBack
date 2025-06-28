package com.tatutaller.dto.response;

import java.math.BigDecimal;

public class PublicClassResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String duration;
    private Integer maxCapacity;
    private String level;
    private String status;
    private String instructorName;
    private String materials;
    private String requirements;

    // Constructors
    public PublicClassResponse() {
    }

    public PublicClassResponse(Long id, String name, String description, BigDecimal price,
            String duration, Integer maxCapacity, String level, String status,
            String instructorName, String materials, String requirements) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.duration = duration;
        this.maxCapacity = maxCapacity;
        this.level = level;
        this.status = status;
        this.instructorName = instructorName;
        this.materials = materials;
        this.requirements = requirements;
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
        return duration;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
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
}
