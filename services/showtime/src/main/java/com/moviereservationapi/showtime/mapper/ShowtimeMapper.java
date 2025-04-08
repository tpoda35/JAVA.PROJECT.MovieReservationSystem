package com.moviereservationapi.showtime.mapper;

import com.moviereservationapi.showtime.dto.showtime.ShowtimeDetailsDtoV1;
import com.moviereservationapi.showtime.dto.showtime.ShowtimeCreateDto;
import com.moviereservationapi.showtime.dto.showtime.ShowtimeDetailsDtoV2;
import com.moviereservationapi.showtime.model.Showtime;

public class ShowtimeMapper {

    public static ShowtimeDetailsDtoV1 fromShowtimeToDetailsDtoV1(Showtime showtime) {
        return ShowtimeDetailsDtoV1.builder()
                .id(showtime.getId())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .build();
    }

    public static Showtime fromManageDtoToShowtime(ShowtimeCreateDto showtimeCreateDto) {
        return Showtime.builder()
                .startTime(showtimeCreateDto.getStartTime())
                .endTime(showtimeCreateDto.getEndTime())
                .movieId(showtimeCreateDto.getMovieId())
                .roomId(showtimeCreateDto.getRoomId())
                .build();
    }

    public static ShowtimeDetailsDtoV2 fromShowtimeToDetailsDtoV2(Showtime showtime) {
        return ShowtimeDetailsDtoV2.builder()
                .id(showtime.getId())
                .roomId(showtime.getRoomId())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .build();
    }

}
