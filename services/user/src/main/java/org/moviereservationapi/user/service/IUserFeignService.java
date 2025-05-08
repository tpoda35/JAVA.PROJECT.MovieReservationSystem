package org.moviereservationapi.user.service;

public interface IUserFeignService {
    void addReservationToUser(Long reservationId);
}
