package com.tatutaller.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.tatutaller.entity.CartItem;
import com.tatutaller.entity.Pedido;
import com.tatutaller.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MercadoPagoService {

    private final PedidoRepository pedidoRepository;

    public MercadoPagoService(
            @Value("${mercadopago.access.token}") String accessToken,
            PedidoRepository pedidoRepository
    ) {
        MercadoPagoConfig.setAccessToken(accessToken);
        this.pedidoRepository = pedidoRepository;
    }

    public Preference crearPreferenciaDesdeCarrito(List<CartItem> carrito, double montoTotalPedido) throws Exception {
        double montoTotalCarrito = carrito.stream()
            .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
            .sum();

        double factorDescuento = (montoTotalCarrito > 0) ? (montoTotalPedido / montoTotalCarrito) : 1.0;

        List<PreferenceItemRequest> items = carrito.stream().map(item ->
            PreferenceItemRequest.builder()
                .id(String.valueOf(item.getId()))
                .title(item.getProduct().getName())
                .description(item.getProduct().getDescription())
                .pictureUrl(item.getProduct().getImageUrl())
                .categoryId(item.getProduct().getCategory() != null ? item.getProduct().getCategory().name() : null)
                .quantity(item.getQuantity())
                .currencyId("UYU")
                .unitPrice(item.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(factorDescuento)))
                .build()
        ).collect(Collectors.toList());

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
            .success("https://tu-frontend.com/pago/success")
            .pending("https://tu-frontend.com/pago/pending")
            .failure("https://tu-frontend.com/pago/failure")
            .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
            .items(items)
            .backUrls(backUrls)
            .autoReturn("approved")
            .build();

        PreferenceClient client = new PreferenceClient();
        return client.create(preferenceRequest);
    }

    public Preference crearPreferenciaPorExternalReference(String externalReference) throws Exception {
        Pedido pedido = pedidoRepository.findByExternalReference(externalReference);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido no encontrado para externalReference: " + externalReference);
        }
        return crearPreferenciaDesdeCarrito(pedido.getItemsSnapshot());
    }
}