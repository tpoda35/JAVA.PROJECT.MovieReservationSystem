package com.moviereservationapi.cinema.utils;

import com.moviereservationapi.cinema.model.Cinema;
import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.model.Seat;
import com.moviereservationapi.cinema.repository.CinemaRepository;
import com.moviereservationapi.cinema.repository.RoomRepository;
import com.moviereservationapi.cinema.repository.SeatRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataSeederConfig {

    @Bean
    CommandLineRunner initDatabase(CinemaRepository cinemaRepository,
                                   RoomRepository roomRepository,
                                   SeatRepository seatRepository) {
        return args -> {
            if (cinemaRepository.count() > 0) {
                System.out.println("Database already seeded. Skipping data initialization.");
                return;
            }

            System.out.println("Starting database seeding...");

            Cinema cinemaOne = new Cinema();
            cinemaOne.setName("Cineplex Odeon");
            cinemaOne.setLocation("Downtown");

            Cinema cinemaTwo = new Cinema();
            cinemaTwo.setName("Silver City");
            cinemaTwo.setLocation("West End");

            // Save cinemas first to get IDs
            cinemaRepository.saveAll(Arrays.asList(cinemaOne, cinemaTwo));

            // Create rooms for first cinema
            Room room1 = new Room();
            room1.setName("IMAX 1");
            room1.setTotalSeat(120);
            room1.setCinema(cinemaOne);

            Room room2 = new Room();
            room2.setName("Standard 1");
            room2.setTotalSeat(80);
            room2.setCinema(cinemaOne);

            // Create rooms for second cinema
            Room room3 = new Room();
            room3.setName("VIP Room");
            room3.setTotalSeat(50);
            room3.setCinema(cinemaTwo);

            Room room4 = new Room();
            room4.setName("3D Experience");
            room4.setTotalSeat(100);
            room4.setCinema(cinemaTwo);

            // Save rooms to get IDs
            List<Room> savedRooms = roomRepository.saveAll(Arrays.asList(room1, room2, room3, room4));

            // Create and save seats for each room
            for (Room room : savedRooms) {
                createAndSaveSeats(room, seatRepository);
            }

            System.out.println("Database seeding completed!");
        };
    }

    private void createAndSaveSeats(Room room, SeatRepository seatRepository) {
        int totalSeats = room.getTotalSeat();
        int seatsPerRow = 20; // Adjust based on your requirements
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};

        for (int i = 0; i < totalSeats; i++) {
            int rowIndex = i / seatsPerRow;
            int seatNumber = i % seatsPerRow + 1;

            // Make sure we don't exceed available rows
            if (rowIndex >= rows.length) {
                break;
            }

            Seat seat = new Seat();
            seat.setSeatRow(rows[rowIndex]);
            seat.setSeatNumber(seatNumber);
            seat.setRoom(room);

            seatRepository.save(seat);
        }
    }

}
