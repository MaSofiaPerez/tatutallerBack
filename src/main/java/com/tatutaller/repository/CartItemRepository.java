package com.tatutaller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tatutaller.entity.Cart;
import com.tatutaller.entity.CartItem;

public interface CartItemRepository  extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    
}
