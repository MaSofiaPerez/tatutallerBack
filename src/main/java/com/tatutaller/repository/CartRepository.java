package com.tatutaller.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tatutaller.entity.Cart;
import com.tatutaller.entity.User;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long id);
    Optional<Cart> findByToken(String token);
    Optional<Cart> findByUser(User user);
    Optional<Cart> findById(User usuario);
}
