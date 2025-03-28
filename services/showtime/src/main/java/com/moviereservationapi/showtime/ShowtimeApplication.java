package com.moviereservationapi.showtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableFeignClients
public class ShowtimeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShowtimeApplication.class, args);
	}

}
