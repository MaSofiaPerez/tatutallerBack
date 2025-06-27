package com.tatutaller.config;

import com.tatutaller.entity.User;
import com.tatutaller.entity.Product;
import com.tatutaller.entity.ClassEntity;
import com.tatutaller.repository.UserRepository;
import com.tatutaller.repository.ProductRepository;
import com.tatutaller.repository.ClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Crear usuario administrador
        if (!userRepository.existsByEmail("admin@tatutaller.com")) {
            User admin = new User();
            admin.setName("Administrador");
            admin.setEmail("admin@tatutaller.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(admin);
        }
        
        // Crear usuario de prueba
        if (!userRepository.existsByEmail("user@test.com")) {
            User user = new User();
            user.setName("Usuario de Prueba");
            user.setEmail("user@test.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(User.Role.USER);
            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);
        }
        
        // Crear usuarios profesores
        if (!userRepository.existsByEmail("teacher1@tatutaller.com")) {
            User teacher1 = new User();
            teacher1.setName("María Rodríguez");
            teacher1.setEmail("teacher1@tatutaller.com");
            teacher1.setPassword(passwordEncoder.encode("teacher123"));
            teacher1.setRole(User.Role.TEACHER);
            teacher1.setStatus(User.UserStatus.ACTIVE);
            teacher1.setPhone("+1234567890");
            teacher1.setAddress("Calle de la Cerámica 123");
            userRepository.save(teacher1);
        }
        
        if (!userRepository.existsByEmail("teacher2@tatutaller.com")) {
            User teacher2 = new User();
            teacher2.setName("Carlos Mendoza");
            teacher2.setEmail("teacher2@tatutaller.com");
            teacher2.setPassword(passwordEncoder.encode("teacher123"));
            teacher2.setRole(User.Role.TEACHER);
            teacher2.setStatus(User.UserStatus.ACTIVE);
            teacher2.setPhone("+0987654321");
            teacher2.setAddress("Avenida del Arte 456");
            userRepository.save(teacher2);
        }
        
        // Crear productos de prueba
        if (productRepository.count() == 0) {
            Product product1 = new Product();
            product1.setName("Arcilla Blanca");
            product1.setDescription("Arcilla de alta calidad para modelado");
            product1.setPrice(new BigDecimal("25.00"));
            product1.setStock(50);
            product1.setCategory(Product.ProductCategory.MATERIALES);
            product1.setStatus(Product.ProductStatus.ACTIVE);
            productRepository.save(product1);
            
            Product product2 = new Product();
            product2.setName("Esmalte Azul Cobalto");
            product2.setDescription("Esmalte cerámico de color azul intenso");
            product2.setPrice(new BigDecimal("35.00"));
            product2.setStock(20);
            product2.setCategory(Product.ProductCategory.MATERIALES);
            product2.setStatus(Product.ProductStatus.ACTIVE);
            productRepository.save(product2);
            
            Product product3 = new Product();
            product3.setName("Torno de Cerámica");
            product3.setDescription("Torno eléctrico profesional para cerámica");
            product3.setPrice(new BigDecimal("1200.00"));
            product3.setStock(3);
            product3.setCategory(Product.ProductCategory.HERRAMIENTAS);
            product3.setStatus(Product.ProductStatus.ACTIVE);
            productRepository.save(product3);
        }
        
        // Crear clases de prueba
        if (classRepository.count() == 0) {
            User teacher1 = userRepository.findByEmail("teacher1@tatutaller.com").orElse(null);
            User teacher2 = userRepository.findByEmail("teacher2@tatutaller.com").orElse(null);
            
            ClassEntity class1 = new ClassEntity();
            class1.setName("Introducción a la Cerámica");
            class1.setDescription("Aprende los fundamentos básicos del trabajo con arcilla");
            class1.setPrice(new BigDecimal("80.00"));
            class1.setDuration("3 horas");
            class1.setMaxCapacity(8);
            class1.setLevel(ClassEntity.ClassLevel.BEGINNER);
            class1.setStatus(ClassEntity.ClassStatus.ACTIVE);
            class1.setInstructor(teacher1);
            class1.setMaterials("Arcilla, herramientas básicas, esmaltes");
            class1.setRequirements("Ninguno - apto para principiantes");
            classRepository.save(class1);
            
            ClassEntity class2 = new ClassEntity();
            class2.setName("Técnicas de Esmaltado");
            class2.setDescription("Domina las técnicas avanzadas de esmaltado cerámico");
            class2.setPrice(new BigDecimal("120.00"));
            class2.setDuration("4 horas");
            class2.setMaxCapacity(6);
            class2.setLevel(ClassEntity.ClassLevel.INTERMEDIATE);
            class2.setStatus(ClassEntity.ClassStatus.ACTIVE);
            class2.setInstructor(teacher2);
            class2.setMaterials("Piezas biscocho, esmaltes variados, pinceles");
            class2.setRequirements("Conocimientos básicos de cerámica");
            classRepository.save(class2);
            
            ClassEntity class3 = new ClassEntity();
            class3.setName("Torno Avanzado");
            class3.setDescription("Perfecciona tu técnica en el torno cerámico");
            class3.setPrice(new BigDecimal("150.00"));
            class3.setDuration("5 horas");
            class3.setMaxCapacity(4);
            class3.setLevel(ClassEntity.ClassLevel.ADVANCED);
            class3.setStatus(ClassEntity.ClassStatus.ACTIVE);
            class3.setInstructor(teacher1);
            class3.setMaterials("Arcilla preparada, acceso a torno, herramientas especializadas");
            class3.setRequirements("Experiencia previa con torno cerámico");
            classRepository.save(class3);
        }
    }
}
