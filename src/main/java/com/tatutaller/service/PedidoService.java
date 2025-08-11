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

    // Determina si el usuario tiene descuento (ejemplo: si está logueado)
    public boolean usuarioTieneDescuentoPorEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public Pedido crearPedidoPorEmail(String email) {
        User usuario = obtenerUsuarioPorEmail(email);
        List<CartItem> carrito = obtenerCarritoActualPorEmail(email);
        boolean aplicarDescuento = usuarioTieneDescuentoPorEmail(email);

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado(Pedido.Estado.PENDING);
        pedido.setItemsSnapshot(List.copyOf(carrito)); // snapshot

        double montoTotal = carrito.stream()
            .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
            .sum();

        if (aplicarDescuento) {
            montoTotal = montoTotal * 0.9; // 10% off
        }

        pedido.setMontoTotal(montoTotal);
        pedido.setExternalReference(UUID.randomUUID().toString());

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> obtenerPedidosPorEmail(String email) {
        User usuario = obtenerUsuarioPorEmail(email);
        return pedidoRepository.findAllByUsuario(usuario);
    }
}