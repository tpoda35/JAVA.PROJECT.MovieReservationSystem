package com.moviereservationapi.movie.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table
public class Review {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "Content field cannot be null.")
    private String content;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    // From another service.

    private Long userId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
