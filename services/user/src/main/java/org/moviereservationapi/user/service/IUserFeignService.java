package org.moviereservationapi.user.service;

import org.moviereservationapi.user.model.AppUser;

public interface IUserFeignService {
    AppUser getLoggedInUserOrCreateIfNotExists();
    void addReservationToUser(Long reservationId);
}
