package com.tatutaller.controller;

import com.tatutaller.entity.Image;
import com.tatutaller.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/imagenes")
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getImage(@PathVariable String id) {
        Optional<Image> imageOpt = imageRepository.findById(id);
        if (imageOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Image image = imageOpt.get();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, image.getMime())
                .body(image.getContenido());
    }
}
