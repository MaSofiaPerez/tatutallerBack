package com.tatutaller.service;

import com.tatutaller.entity.Image;
import com.tatutaller.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    public Image save(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                Image image = new Image();
                image.setMime(file.getContentType());
                image.setNombre(file.getOriginalFilename());
                image.setContenido(file.getBytes());
                return imageRepository.save(image);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    public Image getById(String id) {
        Optional<Image> response = imageRepository.findById(id);
        return response.orElse(null);
    }
    
    
}
