package com.neu.finalproject.meskot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "playback_history")
public class UserHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double position;
    private LocalDateTime watchedAt = LocalDateTime.now();

    @ManyToOne
    private User user;

    @ManyToOne
    private Movie movie;

    // getters and setters
}