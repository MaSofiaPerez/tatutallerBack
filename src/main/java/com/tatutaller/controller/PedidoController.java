package com.tatutaller.controller;

import com.tatutaller.entity.Pedido;
import com.tatutaller.dto.response.PedidoResponse;
import com.tatutaller.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/public/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<?> crearPedido(@Valid @RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email es requerido"));
        }
        try {
            Pedido pedido = pedidoService.crearPedidoPorEmail(email);
            PedidoResponse pedidoResponse = new PedidoResponse(pedido);

            return ResponseEntity.ok(Map.of(
                "pedido", pedidoResponse,
                "externalReference", pedido.getExternalReference()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "No se pudo crear el pedido",
                "details", e.getMessage()
            ));
        }
    }
}