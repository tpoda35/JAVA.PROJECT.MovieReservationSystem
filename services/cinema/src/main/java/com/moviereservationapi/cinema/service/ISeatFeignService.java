package com.moviereservationapi.cinema.service;

import java.util.List;

public interface ISeatFeignService {
    Boolean seatsExists(List<Long> seatIds);
}
