package com.moviereservationapi.movie.feign;

import com.moviereservationapi.movie.feignResponse.AppUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.CompletableFuture;

@FeignClient("USER-SERVICE")
public interface UserClient {

    @GetMapping("/getUser/{userid}")
    CompletableFuture<AppUserResponse> getUser(@PathVariable("userid") Long userId);

}
