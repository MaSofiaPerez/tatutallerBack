package com.tatutaller.repository;

import com.tatutaller.entity.Pedido;
import com.tatutaller.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Pedido findByExternalReference(String externalReference);
    List<Pedido> findAllByUsuario(User usuario);
    List<Pedido> findByUsuarioId(Long id);
    
}