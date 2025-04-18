package com.moviereservationapi.showtime.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Showtime {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long movieId;

    private Long roomId;

    @ElementCollection
    @CollectionTable(
            name = "showtime_reservations",
            joinColumns = @JoinColumn(name = "showtime_id")
    )
    @Column(name = "reservation_id")
    @ToString.Exclude
    private List<Long> reservationIds;

    @Version
    @ToString.Exclude
    private Long version;

}
