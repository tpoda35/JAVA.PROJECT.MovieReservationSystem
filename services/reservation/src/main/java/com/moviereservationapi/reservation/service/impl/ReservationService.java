package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.dto.*;
import com.moviereservationapi.reservation.exception.UserNotFoundException;
import com.moviereservationapi.reservation.feign.CinemaClient;
import com.moviereservationapi.reservation.feign.ShowtimeClient;
import com.moviereservationapi.reservation.mapper.ReservationMapper;
import com.moviereservationapi.reservation.model.Reservation;
import com.moviereservationapi.reservation.model.ReservationSeat;
import com.moviereservationapi.reservation.model.User;
import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.repository.ReservationSeatRepository;
import com.moviereservationapi.reservation.repository.UserRepository;
import com.moviereservationapi.reservation.service.IReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.moviereservationapi.reservation.Enum.PaymentStatus.PENDING;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService implements IReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final UserRepository userRepository;
    private final ShowtimeClient showtimeClient;
    private final CinemaClient cinemaClient;

    @Override
    public ReservationResponseDto addReservation(ReservationManageDto reservationManageDto) {
        Long userId = reservationManageDto.getUserId();
        Long showtimeId = reservationManageDto.getShowtimeId();
        List<Long> seatIds = reservationManageDto.getSeatIds();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        ShowtimeDto showtimeDto = showtimeClient.getShowtime(showtimeId);
        List<SeatDto> seatDtos = cinemaClient.getSeats(seatIds);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setShowtimeId(showtimeId);
        reservation.setPaymentStatus(PENDING);

        List<ReservationSeat> reservationSeats = seatIds.stream()
                .map(seatId -> ReservationSeat.builder()
                        .seatId(seatId)
                        .reservation(reservation)
                        .build())
                .collect(Collectors.toList());
        reservation.setReservationSeats(reservationSeats);

        reservationRepository.save(reservation);
        reservationSeatRepository.saveAll(reservationSeats);



        return ReservationMapper.toReservationResponseDto(reservation, showtimeDto, seatDtos);
    }

    @Override
    public CompletableFuture<ReservationDto> getReservation(Long reservationId) {
        return null;
    }

    @Override
    public void deleteReservation(Long reservationId) {

    }

    @Override
    public CompletableFuture<Page<ReservationDto>> getUserReservations(int pageNum, int pageSize, Long userId) {
        return null;
    }
}
