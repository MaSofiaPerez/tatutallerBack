package com.tatutaller.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.tatutaller.entity.CartItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MercadoPagoService {

    public MercadoPagoService(@Value("${mercadopago.access.token}") String accessToken) {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public Preference crearPreferenciaDesdeCarrito(List<CartItem> carrito) throws Exception {
        List<PreferenceItemRequest> items = carrito.stream().map(item ->
            PreferenceItemRequest.builder()
                .id(String.valueOf(item.getId()))
                .title(item.getProduct().getName())
                .description(item.getProduct().getDescription())
                .pictureUrl(item.getProduct().getImageUrl())
                .categoryId(item.getProduct().getCategory() != null ? item.getProduct().getCategory().name() : null)
                .quantity(item.getQuantity())
                .currencyId("UYU")
                .unitPrice(item.getProduct().getPrice())
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
            .autoReturn("approved") // Redirección automática si el pago es aprobado
            .build();

        PreferenceClient client = new PreferenceClient();
        return client.create(preferenceRequest);
    }
}