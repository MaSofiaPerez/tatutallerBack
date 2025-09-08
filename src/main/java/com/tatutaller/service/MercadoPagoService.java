package com.tatutaller.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
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

    public Preference crearPreferenciaDesdeCarrito(List<CartItem> items, double montoTotal) throws MPException, MPApiException {
        List<PreferenceItemRequest> mpItems = items.stream().map((CartItem item) -> 
            PreferenceItemRequest.builder()
                .title(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getProduct().getPrice())
                .build()
        ).collect(Collectors.toList());

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
            .success("https://www.tatutaller.com.uy/pago/success")
            .pending("https://www.tatutaller.com.uy/pago/pending")
            .failure("https://www.tatutaller.com.uy/pago/failure")
            .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
            .items(mpItems)
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
        return crearPreferenciaDesdeCarrito(pedido.getItemsSnapshot(),pedido.getMontoTotal());
    }
}