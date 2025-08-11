package com.tatutaller.controller;

import com.tatutaller.entity.Pedido;
import com.tatutaller.entity.CartItem;
import com.tatutaller.entity.User;
import com.tatutaller.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/public/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // Endpoint para crear un pedido y vincularlo con Mercado Pago
    @PostMapping
    public ResponseEntity<?> crearPedido(@Valid @RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email es requerido"));
        }

        Pedido pedido = pedidoService.crearPedidoPorEmail(email);

        // Retornar entidad completa y externalReference para Mercado Pago
        return ResponseEntity.ok(Map.of(
            "pedido", pedido,
            "externalReference", pedido.getExternalReference()
        ));
    }
}