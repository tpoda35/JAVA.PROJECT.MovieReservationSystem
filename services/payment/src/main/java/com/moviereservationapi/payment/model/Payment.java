package com.moviereservationapi.payment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table (
        indexes = {
                @Index(name = "idx_showtime_id", columnList = "showtimeId")
        }
)
public class Payment {

    @Id
    @GeneratedValue
    private Long id;

    private Long showtimeId;

    @ElementCollection
    @CollectionTable(
            name = "payment_seats",
            joinColumns = @JoinColumn(name = "payment_id")
    )
    @Column(name = "seat_id")
    @ToString.Exclude
    private List<Long> seatIds = new ArrayList<>();

    private String userId;

    private String userMail;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
