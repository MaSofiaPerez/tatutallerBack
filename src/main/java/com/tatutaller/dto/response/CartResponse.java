
package com.tatutaller.dto.response;

import java.util.List;

public class CartResponse {
    private Long id;
    private String token;
    private String status;
    private List<CartItemResponse> items;

    public CartResponse(Long id, String token, String status, List<CartItemResponse> items) {
        this.id = id;
        this.token = token;
        this.status = status;
        this.items = items;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public String getStatus() { return status; }
    public List<CartItemResponse> getItems() { return items; }
}