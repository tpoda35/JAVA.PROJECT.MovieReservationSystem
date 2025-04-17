package com.moviereservationapi.showtime.dto.showtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowtimeDetailsDtoV2 {

    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long roomId;
}
