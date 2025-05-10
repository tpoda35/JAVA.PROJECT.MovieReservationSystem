package com.moviereservationapi.payment.service;

import com.moviereservationapi.payment.dto.payment.StripeResponse;
import com.moviereservationapi.payment.dto.reservation.ReservationPayment;

public interface IPaymentService {

    StripeResponse checkout(ReservationPayment reservationData, String currency);

}
