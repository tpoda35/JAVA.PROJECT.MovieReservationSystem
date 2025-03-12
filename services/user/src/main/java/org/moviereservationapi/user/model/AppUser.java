package org.moviereservationapi.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class AppUser {

    @Id
    @GeneratedValue
    private Long id;

    @Email(message = "Invalid email format.")
    @NotBlank(message = "Email field cannot be empty.")
    private String email;

    @ElementCollection
    @CollectionTable(
            name = "user_movies",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "movie_id")
    @ToString.Exclude
    private List<Long> movieIds;

    @ElementCollection
    @CollectionTable(
            name = "user_reviews",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "review_id")
    @ToString.Exclude
    private List<Long> reviewIds;

    @ElementCollection
    @CollectionTable(
            name = "user_payments",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "payment_id")
    @ToString.Exclude
    private List<Long> paymentIds;
}
