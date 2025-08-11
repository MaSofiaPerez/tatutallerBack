package com.tatutaller.dto.response;

import com.tatutaller.entity.Pedido;
import java.util.List;

public class PedidoResponse {
    public Long id;
    public String estado;
    public Double montoTotal;
    public String externalReference;
    public List<CartItemResponse> itemsSnapshot;

    public PedidoResponse(Pedido pedido) {
        this.id = pedido.getId();
        this.estado = pedido.getEstado().name();
        this.montoTotal = pedido.getMontoTotal();
        this.externalReference = pedido.getExternalReference();
        // Mapear itemsSnapshot a DTO si es necesario
    }
}