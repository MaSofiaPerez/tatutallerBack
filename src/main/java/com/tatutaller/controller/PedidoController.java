package com.tatutaller.controller;

import com.tatutaller.entity.Pedido;
import com.tatutaller.dto.response.PedidoResponse;
import com.tatutaller.service.PedidoService;
import com.tatutaller.service.MercadoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final MercadoPagoService mercadoPagoService;

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
            // 1. Crear el pedido
            Pedido pedido = pedidoService.crearPedidoPorEmail(email);
            PedidoResponse pedidoResponse = new PedidoResponse(pedido);

            // 2. Crear preferencia de Mercado Pago directamente
             var preferencia = mercadoPagoService.crearPreferenciaDesdeCarrito(pedido.getItemsSnapshot(),pedido.getMontoTotal());
            if (preferencia == null || preferencia.getInitPoint() == null) {
             return ResponseEntity.badRequest().body(Map.of("error", "No se pudo generar la preferencia de pago"));
            }

            // 3. Devolver ambos datos al frontend
            return ResponseEntity.ok(Map.of(
                "pedido", pedidoResponse,
                "externalReference", pedido.getExternalReference(),
                "init_point", preferencia.getInitPoint()
            ));
        } catch (Exception e) {
            e.printStackTrace(); // <--- Agrega esto para ver el error real en la consola
            return ResponseEntity.status(500).body(Map.of(
                "error", "No se pudo crear el pedido y la preferencia",
                "details", e.getMessage()
            ));
        }
     }

// @PostMapping("/checkout")
// public ResponseEntity<?> crearPedidoYPreferencia(@Valid @RequestBody Map<String, Object> payload) {
//     String email = (String) payload.get("email");
//     if (email == null || email.isBlank()) {
//         return ResponseEntity.badRequest().body(Map.of("error", "Email es requerido"));
//     }
//     try {
//         // 1. Crear el pedido en base al email recibido
//         Pedido pedido = pedidoService.crearPedidoPorEmail(email);
//         PedidoResponse pedidoResponse = new PedidoResponse(pedido);

//         // 2. Crear la preferencia de Mercado Pago usando los items y el monto total del pedido
//         var preferencia = mercadoPagoService.crearPreferenciaDesdeCarrito(
//             pedido.getItemsSnapshot(),
//             pedido.getMontoTotal()
//         );
//         // 3. Validar que la preferencia fue creada correctamente
//         if (preferencia == null || preferencia.getSandboxInitPoint() == null) {
//             return ResponseEntity.badRequest().body(Map.of("error", "No se pudo generar la preferencia de pago"));
//         }

//         // 4. Devolver ambos datos al frontend, usando el sandbox_init_point para entorno de pruebas
//         return ResponseEntity.ok(Map.of(
//             "pedido", pedidoResponse,
//             "externalReference", pedido.getExternalReference(),
//             "init_point", preferencia.getSandboxInitPoint()
//         ));
//     } catch (Exception e) {
//         return ResponseEntity.status(500).body(Map.of(
//             "error", "No se pudo crear el pedido y la preferencia",
//             "details", e.getMessage()
//         ));
//     }
// }

}