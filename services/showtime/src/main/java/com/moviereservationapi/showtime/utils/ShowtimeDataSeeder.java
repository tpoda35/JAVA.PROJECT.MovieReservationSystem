package com.moviereservationapi.showtime.utils;

import com.moviereservationapi.showtime.model.Showtime;
import com.moviereservationapi.showtime.repository.ShowtimeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Configuration
public class ShowtimeDataSeeder {

    @Bean
    @Profile("dev")
    public CommandLineRunner seedShowtimes(ShowtimeRepository showtimeRepository) {
        return args -> {
            // Check if data already exists to avoid duplicate seeding
            if (showtimeRepository.count() > 0) {
                System.out.println("Showtime data already exists, skipping seeding");
                return;
            }

            System.out.println("Seeding showtime data...");

            // Sample movie IDs
            List<Long> movieIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);

            // Sample room IDs
            List<Long> roomIds = Arrays.asList(1L, 2L, 3L);

            // Starting with today's date
            LocalDate startDate = LocalDate.now();

            // Create showtimes for next 7 days
            List<Showtime> showtimes = new ArrayList<>();
            Random random = new Random();

            for (int day = 0; day < 7; day++) {
                LocalDate currentDate = startDate.plusDays(day);

                // For each room
                for (Long roomId : roomIds) {
                    // Each room has 3-5 showtimes per day
                    int showtimesPerDay = random.nextInt(3) + 3; // 3 to 5 showtimes

                    for (int i = 0; i < showtimesPerDay; i++) {
                        // Generate random start time between 10:00 and 22:00
                        int startHour = 10 + random.nextInt(12);
                        int startMinute = random.nextInt(4) * 15; // 0, 15, 30, or 45 minutes

                        LocalTime startTimeOfDay = LocalTime.of(startHour, startMinute);
                        LocalDateTime startDateTime = LocalDateTime.of(currentDate, startTimeOfDay);

                        // Movie duration between 90 and 180 minutes
                        int movieDuration = 90 + random.nextInt(7) * 15; // 90 to 180 minutes in 15-minute increments
                        LocalDateTime endDateTime = startDateTime.plusMinutes(movieDuration);

                        // Select a random movie
                        Long movieId = movieIds.get(random.nextInt(movieIds.size()));

                        // Create the showtime
                        Showtime showtime = new Showtime();
                        showtime.setStartTime(startDateTime);
                        showtime.setEndTime(endDateTime);
                        showtime.setMovieId(movieId);
                        showtime.setRoomId(roomId);

                        showtimes.add(showtime);
                    }
                }
            }

            showtimeRepository.saveAll(showtimes);

            System.out.println("Successfully seeded " + showtimes.size() + " showtimes");
        };
    }

}
