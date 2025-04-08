package com.moviereservationapi.reservation.model;

import com.moviereservationapi.reservation.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        indexes = {
                @Index(name = "idx_reservation_showtime_id", columnList = "showtimeId")
        }
)
public class Reservation {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime reservationTime;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ReservationSeat> reservationSeats;

    // Feign
    private Long showtimeId;

    @PrePersist
    protected void onCreate() {
        reservationTime = LocalDateTime.now();
    }
}
