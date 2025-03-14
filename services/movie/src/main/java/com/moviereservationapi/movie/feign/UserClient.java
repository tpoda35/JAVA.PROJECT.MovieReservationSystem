package com.moviereservationapi.movie.feign;

import com.moviereservationapi.movie.feignResponse.AppUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FeignClient("USER-SERVICE")
public interface UserClient {

    // problem
    @GetMapping("/getUsers")
    CompletableFuture<List<AppUserDto>> getUsers(@RequestParam List<Long> userIds);

}
