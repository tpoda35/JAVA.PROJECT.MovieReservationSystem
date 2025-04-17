package com.moviereservationapi.reservation.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowtimeDto {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long roomId;

}
