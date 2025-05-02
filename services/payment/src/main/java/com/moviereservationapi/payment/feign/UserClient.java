package com.moviereservationapi.payment.feign;

import com.moviereservationapi.payment.dto.feign.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "user-service", url = "http://localhost:8096")
public interface UserClient {

    @PostMapping("/api/users/feign")
    UserDto getLoggedInUser();

}
