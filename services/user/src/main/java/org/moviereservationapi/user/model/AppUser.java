package org.moviereservationapi.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
