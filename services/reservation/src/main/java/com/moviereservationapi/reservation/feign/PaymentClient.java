package com.moviereservationapi.reservation.feign;

import com.moviereservationapi.reservation.dto.feign.StripeResponse;
import com.moviereservationapi.reservation.dto.reservation.ReservationPayment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-service", url = "http://localhost:8095")
public interface PaymentClient {

    @PostMapping("/api/payments")
    StripeResponse checkout(
            @RequestParam(defaultValue = "EUR") String currency,
            @RequestBody ReservationPayment reservationData
    );

}
