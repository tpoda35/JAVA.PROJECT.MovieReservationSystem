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
                @Index(name = "idx_reservation_showtime_id", columnList = "showtimeId"),
                @Index(name = "idx_reservation_expires_at", columnList = "expiresAt")
        }
)
public class Reservation {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime reservationTime;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    // Feign
    private String userId;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ReservationSeat> reservationSeats;

    // Feign
    private Long showtimeId;

    private LocalDateTime expiresAt;

    @Version
    @ToString.Exclude
    private Long version;

    @PrePersist
    protected void onCreate() {
        reservationTime = LocalDateTime.now();
        expiresAt = reservationTime.plusMinutes(30);
    }
}
