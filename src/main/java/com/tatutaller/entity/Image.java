package com.tatutaller.entity;

import jakarta.persistence.*;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_imagen", nullable = false, unique = true)
    private String id;

    private String mime;
    private String nombre;

    @Lob
    @Column(name = "contenido", columnDefinition = "LONGBLOB")
    private byte[] contenido;

    // Constructor vacío, getters y setters
    public Image() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMime() { return mime; }
    public void setMime(String mime) { this.mime = mime; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public byte[] getContenido() { return contenido; }
    public void setContenido(byte[] contenido) { this.contenido = contenido; }
}