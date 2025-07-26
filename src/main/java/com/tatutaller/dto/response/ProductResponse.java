package com.tatutaller.dto.response;

import java.math.BigDecimal;

import com.tatutaller.entity.Product.ProductCategory;
import com.tatutaller.entity.Product.ProductStatus;

public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private ProductCategory category;
    private ProductStatus status;
    private String createdAt;
    private String updatedAt;

    public ProductResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            String imageUrl,
            ProductCategory category,
            ProductStatus status,
            String createdAt,
            String updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters...
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public ProductCategory getCategory() { return category; }
    public ProductStatus getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}