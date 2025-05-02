package org.moviereservationapi.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class AppUser {

    @Id
    private String id;
    private String email;

    @ElementCollection
    @CollectionTable(
            name = "appUser_reservations",
            joinColumns = @JoinColumn(name = "appUser_id")
    )
    @Column(name = "reservation_id")
    @ToString.Exclude
    private List<Long> reservationIds = new ArrayList<>();

}
