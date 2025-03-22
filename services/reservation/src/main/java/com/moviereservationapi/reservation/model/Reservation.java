package com.moviereservationapi.reservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Reservation {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime reservationTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationSeat> reservationSeats;

    // Feign
    private Long showtimeId;

    @PrePersist
    protected void onCreate() {
        reservationTime = LocalDateTime.now();
    }
}
