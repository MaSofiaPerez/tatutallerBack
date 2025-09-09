package com.tatutaller.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import com.tatutaller.repository.CartItemRepository;

@Entity
@Table(name = "pedidos")
public class Pedido {

    public enum Estado {
        PENDING, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "pedido_id")
    private List<CartItem> itemsSnapshot;

    @Column(nullable = false)
    private Double montoTotal;

    @Column(unique = true)
    private String externalReference;

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }

    // Getters y setters

    public Long getId() { return id; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    public LocalDateTime getFecha() { return fecha; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public List<CartItem> getItemsSnapshot() { return itemsSnapshot; }
    public void setItemsSnapshot(List<CartItem> itemsSnapshot) { this.itemsSnapshot = itemsSnapshot; }
    public Double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public void deleteItems(CartItemRepository cartItemRepository) {
        for (CartItem item : itemsSnapshot) {
            cartItemRepository.delete(item);
        }
    }
}