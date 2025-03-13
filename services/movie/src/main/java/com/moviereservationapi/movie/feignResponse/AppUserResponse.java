package com.moviereservationapi.movie.feignResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppUserResponse {

    private String email;
    private List<Long> movieIds;
    private List<Long> reviewIds;
    private List<Long> paymentIds;

}
