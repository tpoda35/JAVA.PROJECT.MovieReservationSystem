package com.moviereservationapi.payment.service;

import com.moviereservationapi.payment.dto.ReservationPaymentRequest;
import com.moviereservationapi.payment.dto.StripeResponse;

public interface IPaymentService {

    StripeResponse checkout(ReservationPaymentRequest request);

}
