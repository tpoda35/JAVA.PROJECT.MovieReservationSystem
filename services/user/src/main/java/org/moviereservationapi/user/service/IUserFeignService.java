package org.moviereservationapi.user.service;

import org.moviereservationapi.user.model.AppUser;

public interface IUserFeignService {
    AppUser getLoggedInUser();
    void addReservationToUser(Long reservationId);
}
