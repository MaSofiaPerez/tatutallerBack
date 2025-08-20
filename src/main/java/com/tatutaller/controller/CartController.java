package com.tatutaller.controller;

import com.tatutaller.entity.Cart;
import com.tatutaller.entity.CartItem;
import com.tatutaller.entity.Product;
import com.tatutaller.entity.User;
import com.tatutaller.dto.response.CartResponse;
import com.tatutaller.dto.response.CartItemResponse;
import com.tatutaller.dto.response.ProductResponse;
import com.tatutaller.repository.CartItemRepository;
import com.tatutaller.repository.CartRepository;
import com.tatutaller.repository.ProductRepository;
import com.tatutaller.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @GetMapping
    public ResponseEntity<?> getCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String cartToken) {
        Cart cart = null;
        if (userDetails != null) {
            Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
            if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
            User user = userOpt.get();
            cart = cartRepository.findByUserId(user.getId()).orElse(null);
        } else if (cartToken != null && !cartToken.isBlank()) {
            cart = cartRepository.findByToken(cartToken).orElse(null);
        }
        if (cart == null) return ResponseEntity.notFound().build();

        List<CartItemResponse> items = cart.getItems() == null ? List.of() :
                cart.getItems().stream()
                        .filter(item -> item != null && item.getProduct() != null)
                        .map(item -> new CartItemResponse(
                                item.getId(),
                                new ProductResponse(
                                        item.getProduct().getId(),
                                        item.getProduct().getName(),
                                        item.getProduct().getDescription(),
                                        item.getProduct().getPrice(),
                                        item.getProduct().getStock(),
                                        item.getProduct().getImageUrl(),
                                        item.getProduct().getCategory(),
                                        item.getProduct().getStatus(),
                                        item.getProduct().getCreatedAt() != null ? item.getProduct().getCreatedAt().toString() : null,
                                        item.getProduct().getUpdatedAt() != null ? item.getProduct().getUpdatedAt().toString() : null,
                                        item.getProduct().getCantidadProducto()
                                ),
                                item.getQuantity()
                        ))
                        .toList();

        CartResponse cartResponse = new CartResponse(
                cart.getId(),
                cart.getToken(),
                cart.getStatus().name(),
                items
        );

        return ResponseEntity.ok(Map.of(
                "cart", cartResponse,
                "cartToken", cart.getToken()
        ));
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addProductToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) String cartToken) {

        Cart cart = null;
        User user = null;

        if (userDetails != null) {
            Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
            if (userOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Usuario no encontrado"));
            user = userOpt.get();
            cart = cartRepository.findByUserId(user.getId()).orElse(null);
        } else if (cartToken != null && !cartToken.isBlank()) {
            cart = cartRepository.findByToken(cartToken).orElse(null);
        }

        // Validar que no se cree un carrito con un token duplicado
        if (cart == null) {
            if (cartToken != null && !cartToken.isBlank()) {
                // Si el token ya existe, no crear un nuevo carrito, devolver el existente
                Optional<Cart> existingCart = cartRepository.findByToken(cartToken);
                if (existingCart.isPresent()) {
                    cart = existingCart.get();
                } else {
                    cart = new Cart();
                    cart.setUser(user); // null si es anónimo
                    cart.setToken(cartToken);
                    cartRepository.save(cart);
                }
            } else {
                // Generar un token único
                String newToken = java.util.UUID.randomUUID().toString();
                cart = new Cart();
                cart.setUser(user); // null si es anónimo
                cart.setToken(newToken);
                cartRepository.save(cart);
                cartToken = newToken;
            }
        } else if (cart.getToken() == null || cart.getToken().isBlank()) {
            // Si el carrito existe pero no tiene token, asígnale uno
            String newToken = java.util.UUID.randomUUID().toString();
            cart.setToken(newToken);
            cartRepository.save(cart);
            cartToken = newToken;
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Producto no encontrado"));
        Product product = productOpt.get();

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

        List<CartItemResponse> items = cart.getItems() == null ? List.of() :
                cart.getItems().stream()
                        .filter(ci -> ci != null && ci.getProduct() != null)
                        .map(ci -> new CartItemResponse(
                                ci.getId(),
                                new ProductResponse(
                                        ci.getProduct().getId(),
                                        ci.getProduct().getName(),
                                        ci.getProduct().getDescription(),
                                        ci.getProduct().getPrice(),
                                        ci.getProduct().getStock(),
                                        ci.getProduct().getImageUrl(),
                                        ci.getProduct().getCategory(),
                                        ci.getProduct().getStatus(),
                                        ci.getProduct().getCreatedAt() != null ? ci.getProduct().getCreatedAt().toString() : null,
                                        ci.getProduct().getUpdatedAt() != null ? ci.getProduct().getUpdatedAt().toString() : null,
                                        ci.getProduct().getCantidadProducto()
                                ),
                                ci.getQuantity()
                        ))
                        .toList();

        CartResponse cartResponse = new CartResponse(
                cart.getId(),
                cart.getToken(),
                cart.getStatus().name(),
                items
        );

        return ResponseEntity.ok(Map.of(
                "cart", cartResponse,
                "cartToken", cart.getToken()
        ));
    }

    @PutMapping("/item/{itemId}")
    @Transactional
    public ResponseEntity<?> updateCartItemQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam int quantity,
            @RequestParam(required = false) String cartToken) {

        if (quantity < 1) return ResponseEntity.badRequest().body(Map.of("message", "Cantidad inválida"));

        Optional<CartItem> itemOpt = cartItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return ResponseEntity.notFound().build();

        CartItem item = itemOpt.get();
        Cart cart = item.getCart();

        boolean autorizado = false;
        if (userDetails != null && cart.getUser() != null) {
            Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
            autorizado = userOpt.isPresent() && cart.getUser().getId().equals(userOpt.get().getId());
        } else if (cartToken != null && cartToken.equals(cart.getToken())) {
            autorizado = true;
        }
        if (!autorizado) return ResponseEntity.status(403).body(Map.of("message", "No autorizado"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        CartItemResponse itemResponse = new CartItemResponse(
                item.getId(),
                new ProductResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getDescription(),
                        item.getProduct().getPrice(),
                        item.getProduct().getStock(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getCategory(),
                        item.getProduct().getStatus(),
                        item.getProduct().getCreatedAt() != null ? item.getProduct().getCreatedAt().toString() : null,
                        item.getProduct().getUpdatedAt() != null ? item.getProduct().getUpdatedAt().toString() : null,
                        item.getProduct().getCantidadProducto()
                ),
                item.getQuantity()
        );
        return ResponseEntity.ok(itemResponse);
    }

    @DeleteMapping("/item/{itemId}")
    @Transactional
    public ResponseEntity<?> removeCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam(required = false) String cartToken) {

        Optional<CartItem> itemOpt = cartItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return ResponseEntity.notFound().build();

        CartItem item = itemOpt.get();
        Cart cart = item.getCart();

        boolean autorizado = false;
        if (userDetails != null && cart.getUser() != null) {
            Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
            autorizado = userOpt.isPresent() && cart.getUser().getId().equals(userOpt.get().getId());
        } else if (cartToken != null && cartToken.equals(cart.getToken())) {
            autorizado = true;
        }
        if (!autorizado) return ResponseEntity.status(403).body(Map.of("message", "No autorizado"));

        cartItemRepository.deleteById(itemId);
        return ResponseEntity.ok(Map.of("message", "Item eliminado"));
    }

    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String cartToken) {

        Cart cart = null;
        if (userDetails != null) {
            Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
            if (userOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Usuario no encontrado"));
            User user = userOpt.get();
            cart = cartRepository.findByUserId(user.getId()).orElse(null);
        } else if (cartToken != null && !cartToken.isBlank()) {
            cart = cartRepository.findByToken(cartToken).orElse(null);
        }
        if (cart == null) return ResponseEntity.badRequest().body(Map.of("message", "Carrito no encontrado"));

        cart.getItems().forEach(cartItemRepository::delete);
        cart.getItems().clear();
        cartRepository.save(cart);

        return ResponseEntity.ok(Map.of("message", "Carrito vaciado"));
    }
}
