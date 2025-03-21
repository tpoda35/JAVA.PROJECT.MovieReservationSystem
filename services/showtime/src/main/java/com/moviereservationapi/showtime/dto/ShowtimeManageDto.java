package com.moviereservationapi.showtime.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowtimeManageDto {

    @NotNull(message = "Start time field cannot be empty.")
    private LocalDateTime startTime;

    @NotNull(message = "End time field cannot be empty.")
    private LocalDateTime endTime;

    @NotNull(message = "MovieId field cannot be empty.")
    private Long movieId;

    @NotNull(message = "RoomId field cannot be empty.")
    private Long roomId;

}
