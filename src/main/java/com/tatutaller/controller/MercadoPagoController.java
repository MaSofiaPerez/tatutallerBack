package com.tatutaller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tatutaller.service.MercadoPagoService;

import java.util.Map;

@RestController
@RequestMapping("/api/public/mercadopago")
public class MercadoPagoController {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @PostMapping("/preferencia")
    public ResponseEntity<?> crearPreferencia(@RequestBody Map<String, String> payload) {
        String externalReference = payload.get("externalReference");
        if (externalReference == null || externalReference.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "externalReference es requerido"));
        }
        try {
            // Busca el pedido por externalReference
            var preferencia = mercadoPagoService.crearPreferenciaPorExternalReference(externalReference);
            if (preferencia == null || preferencia.getInitPoint() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pudo generar la preferencia de pago"));
            }
            return ResponseEntity.ok(Map.of("init_point", preferencia.getInitPoint()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al crear preferencia de Mercado Pago",
                "details", e.getMessage()
            ));
        }
    }
}
