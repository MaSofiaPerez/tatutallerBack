package com.tatutaller.dto.response;

public class CartItemResponse {
    private Long id;
    private ProductResponse product;
    private int quantity;

    public CartItemResponse(Long id, ProductResponse product, int quantity) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public ProductResponse getProduct() { return product; }
    public int getQuantity() { return quantity; }
}