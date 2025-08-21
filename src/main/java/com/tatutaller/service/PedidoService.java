package com.tatutaller.service;

import com.tatutaller.entity.Pedido;
import com.tatutaller.entity.CartItem;
import com.tatutaller.entity.User;
import com.tatutaller.repository.PedidoRepository;
import com.tatutaller.repository.UserRepository;
import com.tatutaller.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    // Obtiene el usuario por email
    public User obtenerUsuarioPorEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // Obtiene el carrito actual del usuario por email
    public List<CartItem> obtenerCarritoActualPorEmail(String email) {
        User usuario = obtenerUsuarioPorEmail(email);
        return cartRepository.findByUser(usuario)
                .map(cart -> cart.getItems())
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
    }

    @Transactional
    public Pedido crearPedidoPorEmail(String email) {
        User usuario = obtenerUsuarioPorEmail(email);
        List<CartItem> carrito = obtenerCarritoActualPorEmail(email);

        // Copia los ítems del carrito al snapshot (sin asociar al Cart original)
        List<CartItem> snapshot = carrito.stream()
            .map(item -> {
                CartItem snap = new CartItem();
                snap.setProduct(item.getProduct());
                snap.setQuantity(item.getQuantity());
                // No asocies el snapshot a un Cart, solo al Pedido
                return snap;
            })
            .toList();

        // Aplica descuento si el usuario NO es CLIENTE
        boolean aplicarDescuento = usuario.getRole() != User.Role.CLIENTE;

        double montoTotal = carrito.stream()
            .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
            .sum();

        if (aplicarDescuento) {
            montoTotal = montoTotal * 0.9; // 10% de descuento
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado(Pedido.Estado.PENDING);
        pedido.setItemsSnapshot(snapshot);
        pedido.setMontoTotal(montoTotal);
        pedido.setExternalReference(UUID.randomUUID().toString());

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> obtenerPedidosPorEmail(String email) {
        User usuario = obtenerUsuarioPorEmail(email);
        return pedidoRepository.findAllByUsuario(usuario);
    }

    public Optional<Pedido> obtenerPedidoConSnapshot(Long pedidoId) {
        return pedidoRepository.findById(pedidoId);
        // El snapshot está en pedido.getItemsSnapshot()
    }

    public List<Pedido> obtenerTodosPedidos() {
        return pedidoRepository.findAll();
    }

    public void guardarPedido(Pedido pedido) {
        pedidoRepository.save(pedido);
    }

    public void eliminarPedido(Long id) {
        pedidoRepository.deleteById(id);
    }

    public boolean existePedido(Long id) {
        return pedidoRepository.existsById(id);
    }
}