package com.moviereservationapi.payment.service;

import com.moviereservationapi.payment.dto.payment.StripeResponse;

public interface IPaymentService {

    StripeResponse checkout(Long reservationId, String currency);

}
