package com.tatutaller.controller;

import com.tatutaller.entity.Pedido;
import com.tatutaller.dto.response.PedidoResponse;
import com.tatutaller.service.PedidoService;
import com.tatutaller.service.MercadoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final MercadoPagoService mercadoPagoService;

    @Autowired
    public PedidoController(PedidoService pedidoService, MercadoPagoService mercadoPagoService) {
        this.pedidoService = pedidoService;
        this.mercadoPagoService = mercadoPagoService;
    }

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

    @PostMapping("/checkout")
    public ResponseEntity<?> crearPedidoYPreferencia(@Valid @RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email es requerido"));
        }
        try {
            Pedido pedido = pedidoService.crearPedidoPorEmail(email);
            PedidoResponse pedidoResponse = new PedidoResponse(pedido);

            var preferencia = mercadoPagoService.crearPreferenciaDesdeCarrito(
                pedido.getItemsSnapshot(), pedido.getMontoTotal());
            if (preferencia == null || preferencia.getInitPoint() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pudo generar la preferencia de pago"));
            }

            return ResponseEntity.ok(Map.of(
                "pedido", pedidoResponse,
                "externalReference", pedido.getExternalReference(),
                "init_point", preferencia.getInitPoint()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "error", "No se pudo crear el pedido y la preferencia",
                "details", e.getMessage()
            ));
        }
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PedidoResponse>> getAllPedidos() {
        List<PedidoResponse> pedidos = pedidoService.obtenerTodosPedidos()
            .stream().map(PedidoResponse::new).toList();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPedidoById(@PathVariable Long id) {
        Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoConSnapshot(id);
        if (pedidoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Pedido pedido = pedidoOpt.get();
        return ResponseEntity.ok(new PedidoResponse(pedido));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePedido(@PathVariable Long id, @Valid @RequestBody Pedido updated) {
        Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoConSnapshot(id);
        if (pedidoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Pedido pedido = pedidoOpt.get();

        pedido.setEstado(updated.getEstado());
        pedido.setMontoTotal(updated.getMontoTotal());
        // Puedes agregar más campos editables aquí

        pedidoService.guardarPedido(pedido);
        return ResponseEntity.ok(new PedidoResponse(pedido));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePedido(@PathVariable Long id) {
        if (!pedidoService.existePedido(id)) return ResponseEntity.notFound().build();
        pedidoService.eliminarPedido(id);
        return ResponseEntity.ok(Map.of("message", "Pedido eliminado"));
    }

    @GetMapping("/usuario/{email}")
    public ResponseEntity<List<PedidoResponse>> getPedidosPorUsuario(@PathVariable String email) {
        List<PedidoResponse> pedidos = pedidoService.obtenerPedidosPorEmail(email)
            .stream().map(PedidoResponse::new).toList();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}/snapshot")
    public ResponseEntity<?> getPedidoSnapshot(@PathVariable Long id) {
        return pedidoService.obtenerPedidoConSnapshot(id)
            .map(pedido -> ResponseEntity.ok(Map.of(
                "pedidoId", pedido.getId(),
                "estado", pedido.getEstado(),
                "fecha", pedido.getFecha(),
                "montoTotal", pedido.getMontoTotal(),
                "itemsSnapshot", pedido.getItemsSnapshot()
            )))
            .orElse(ResponseEntity.notFound().build());
    }
}