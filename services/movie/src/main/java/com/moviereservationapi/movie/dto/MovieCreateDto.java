package com.moviereservationapi.movie.dto;

import com.moviereservationapi.movie.Enum.MovieGenre;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieCreateDto {

    @NotNull(message = "Movie title cannot be null.")
    @Length(min = 1, max = 100, message = "Movie name must be between 1 and 100 characters.")
    private String title;

    @NotNull(message = "Movie length cannot be null.")
    private Double length;

    @NotNull(message = "Movie release cannot be null.")
    private LocalDateTime release;

    @Enumerated(EnumType.STRING)
    private MovieGenre movieGenre;

}
