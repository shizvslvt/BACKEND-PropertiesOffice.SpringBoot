package com.example.propertiesoffice.user;

import jakarta.persistence.*;

@Table(name = "users")
@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "mail")
    private String mail;

    public UserEntity() {
    }

    public UserEntity(Long id, String mail) {
        this.id = id;
        this.mail = mail;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
}