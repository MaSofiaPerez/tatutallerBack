package com.tatutaller.controller;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.resources.payment.Payment;
import com.tatutaller.entity.Pedido;
import com.tatutaller.repository.PedidoRepository;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@RestController
@RequestMapping("/api/public/mercadopago")
public class MercadoPagoWebhookController {

    private static final String SECRET_KEY = "823972724c109d29fae8eb61e166e71b2975f98dc73148acf98d845267a77e4d";

    public MercadoPagoWebhookController(@Value("${mercadopago.access.token}") String accessToken) {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    @Autowired
    private PedidoRepository pedidoRepository;

    @PostMapping("/webhook")
    public ResponseEntity<?> recibirWebhook(@RequestBody Map<String, Object> payload, @RequestHeader Map<String, String> headers, @RequestParam Map<String, String> queryParams) {
        String xSignature = headers.get("x-signature");
        String xRequestId = headers.get("x-request-id");
        String dataId = queryParams.getOrDefault("data.id", "");
        String ts = null;
        String hash = null;

        if (xSignature != null) {
            for (String part : xSignature.split(",")) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    if ("ts".equals(keyValue[0].trim())) ts = keyValue[1].trim();
                    if ("v1".equals(keyValue[0].trim())) hash = keyValue[1].trim();
                }
            }
        }
        String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, ts);
        String calculatedHash = new HmacUtils("HmacSHA256", SECRET_KEY).hmacHex(manifest);
        boolean valid = hash != null && hash.equals(calculatedHash);

        try {
            if (dataId != null && !dataId.isEmpty()) {
                RestTemplate restTemplate = new RestTemplate();
                String url = "https://api.mercadopago.com/v1/payments/" + dataId;
                HttpHeaders httpHeaders = new HttpHeaders();
                // Access Token de producción
                httpHeaders.setBearerAuth("APP_USR-1443611189943243-080918-c39ceca6b0f7b5dd0eca5957558699e4-1561567594");
                HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);

                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                Map paymentData = response.getBody();
                String status = (String) paymentData.get("status");
                String externalReference = (String) paymentData.get("external_reference");

                if (externalReference != null) {
                    Pedido pedido = pedidoRepository.findByExternalReference(externalReference);
                    if (pedido != null) {
                        switch (status) {
                            case "approved":
                                pedido.setEstado(Pedido.Estado.APPROVED);
                                break;
                            case "pending":
                                pedido.setEstado(Pedido.Estado.PENDING);
                                break;
                            case "rejected":
                                pedido.setEstado(Pedido.Estado.REJECTED);
                                break;
                        }
                        pedidoRepository.save(pedido);
                        return ResponseEntity.ok(Map.of(
                            "message", "Estado de pedido actualizado",
                            "pedidoId", pedido.getId(),
                            "estado", pedido.getEstado(),
                            "validSignature", valid
                        ));
                    } else {
                        return ResponseEntity.status(404).body(Map.of(
                            "error", "Pedido no encontrado",
                            "externalReference", externalReference,
                            "validSignature", valid
                        ));
                    }
                }
            }
            return ResponseEntity.badRequest().body(Map.of(
                "error", "No se recibió dataId válido",
                "validSignature", valid
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error procesando el webhook",
                "details", e.getMessage(),
                "validSignature", valid
            ));
        }
    }
}