package com.moviereservationapi.showtime.mapper;

import com.moviereservationapi.showtime.dto.ShowtimeDto;
import com.moviereservationapi.showtime.model.Showtime;

public class ShowtimeMapper {

    public static ShowtimeDto fromShowtimeToDto(Showtime showtime) {
        return ShowtimeDto.builder()
                .id(showtime.getId())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .build();
    }

}
