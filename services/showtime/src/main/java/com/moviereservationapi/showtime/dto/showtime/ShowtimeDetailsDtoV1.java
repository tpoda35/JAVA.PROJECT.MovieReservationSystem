package com.moviereservationapi.showtime.dto.showtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowtimeDetailsDtoV1 implements Serializable {

    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
