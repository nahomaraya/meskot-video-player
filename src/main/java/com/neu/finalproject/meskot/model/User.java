package com.neu.finalproject.meskot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String role;
    private String password;
    private String passwordHash;
    private int isAdmin;

    private LocalDateTime createdAt = LocalDateTime.now();

    public User(int i, String username, String email, boolean b) {
    }

    public User() {

    }
}
