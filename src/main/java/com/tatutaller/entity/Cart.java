package com.tatutaller.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonValue;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true,nullable = true)
    private User user; // Ahora puede ser null para carritos anónimos

    @Column(name = "token", unique = true, length = 64)
    private String token; // Token único para carritos anónimos

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference//Estaba haciendo una recursión infinita, ahora se maneja correctamente
    private List<CartItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatus status = CartStatus.ACTIVE;

    public enum CartStatus {
        ACTIVE("Activo"),
        CHECKED_OUT("Finalizado"),
        CANCELLED("Cancelado");

        private final String displayName;

        CartStatus(String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String getDisplayName() {
            return displayName;
        }

        @JsonCreator
        public static CartStatus fromDisplayName(String displayName) {
            for (CartStatus status : CartStatus.values()) {
                if (status.displayName.equalsIgnoreCase(displayName)) {
                    return status;
                }
            }
            // Fallback para valores en inglés
            try {
                return CartStatus.valueOf(displayName.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Valor de estado de carrito no válido: " + displayName);
            }
        }
    }
    
   

    // Getters and setters

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public CartStatus getStatus() { return status; }
    public void setStatus(CartStatus status) { this.status = status; }
}
