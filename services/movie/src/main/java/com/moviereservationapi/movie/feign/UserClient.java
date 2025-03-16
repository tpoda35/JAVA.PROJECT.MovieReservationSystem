package com.moviereservationapi.movie.feign;

import com.moviereservationapi.movie.feignResponse.AppUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "USER-SERVICE", path = "api/users")
public interface UserClient {

    @GetMapping("/getUsers")
    List<AppUserDto> getUsers(@RequestParam List<Long> userIds);

}
