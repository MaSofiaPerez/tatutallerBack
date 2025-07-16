package com.tatutaller.controller;

import com.tatutaller.entity.Cart;
import com.tatutaller.entity.CartItem;
import com.tatutaller.entity.Product;
import com.tatutaller.entity.User;
import com.tatutaller.repository.CartItemRepository;
import com.tatutaller.repository.CartRepository;
import com.tatutaller.repository.ProductRepository;
import com.tatutaller.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    // 1. Obtener el carrito del usuario autenticado
    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cart);
    }

    // 2. Agregar producto al carrito
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addProductToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity) {

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("Usuario no encontrado");
        User user = userOpt.get();

        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) return ResponseEntity.badRequest().body("Carrito no encontrado");

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return ResponseEntity.badRequest().body("Producto no encontrado");
        Product product = productOpt.get();

        // Buscar si ya existe el item
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }
        cartRepository.save(cart);
        return ResponseEntity.ok(cart);
    }

    // 3. Modificar cantidad de un producto
    @PutMapping("/item/{itemId}")
    @Transactional
    public ResponseEntity<?> updateCartItemQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam int quantity) {

        if (quantity < 1) return ResponseEntity.badRequest().body("Cantidad inválida");

        Optional<CartItem> itemOpt = cartItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return ResponseEntity.notFound().build();

        CartItem item = itemOpt.get();
        // Opcional: validar que el item pertenezca al usuario autenticado
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return ResponseEntity.ok(item);
    }

    // 4. Eliminar producto del carrito
    @DeleteMapping("/item/{itemId}")
    @Transactional
    public ResponseEntity<?> removeCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {

        Optional<CartItem> itemOpt = cartItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return ResponseEntity.notFound().build();

        cartItemRepository.deleteById(itemId);
        return ResponseEntity.ok().build();
    }

    // 5. Vaciar carrito
    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("Usuario no encontrado");
        User user = userOpt.get();

        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) return ResponseEntity.badRequest().body("Carrito no encontrado");

        cart.getItems().forEach(item -> cartItemRepository.delete(item));
        cart.getItems().clear();
        cartRepository.save(cart);

        return ResponseEntity.ok().build();
    }
}
