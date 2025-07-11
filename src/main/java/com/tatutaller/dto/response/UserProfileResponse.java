package com.tatutaller.dto.response;

public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String role;

    public UserProfileResponse(Long id, String name, String email, String phone, String address, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
}
