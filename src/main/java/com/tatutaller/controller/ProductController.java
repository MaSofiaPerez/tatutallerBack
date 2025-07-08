package com.tatutaller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tatutaller.dto.request.ProductRequest;
import com.tatutaller.entity.Image;
import com.tatutaller.entity.Product;
import com.tatutaller.repository.ProductRepository;
import com.tatutaller.service.ImageService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ProductRepository productRepository;

    // Endpoint público para obtener productos
    @GetMapping("/public/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAvailableProducts();
        return ResponseEntity.ok(products);
    }

    // Endpoints administrativos
    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getAllProductsAdmin() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/admin/products", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ProductRequest request = mapper.readValue(productJson, ProductRequest.class);

            Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStock()
            );

            // If file is present, save it and set the imageUrl
            if (file != null && !file.isEmpty()) {
                Image image = imageService.save(file);
                if (image != null && image.getId() != null) {
                    product.setImageUrl("/imagenes/" + image.getId());
                }
            } else {
                product.setImageUrl(request.getImageUrl());
            }

            product.setCategory(request.getCategory());
            product.setStatus(request.getStatus());

            Product savedProduct = productRepository.save(product);
            return ResponseEntity.ok(savedProduct);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error creating product", "error", e.getMessage()));
        }
    }

    @PutMapping(value = "/admin/products/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ProductRequest request = mapper.readValue(productJson, ProductRequest.class);

            Optional<Product> productOpt = productRepository.findById(id);
            if (!productOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Product product = productOpt.get();

            // Actualizar campos
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock());
            product.setCategory(request.getCategory());
            product.setStatus(request.getStatus());

            // Si viene archivo, guardar imagen y actualizar URL
            if (file != null && !file.isEmpty()) {
                Image image = imageService.save(file);
                if (image != null && image.getId() != null) {
                    product.setImageUrl("/imagenes/" + image.getId());
                }
            } else {
                product.setImageUrl(request.getImageUrl());
            }

            Product updatedProduct = productRepository.save(product);
            return ResponseEntity.ok(updatedProduct);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error al actualizar el producto", "error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
