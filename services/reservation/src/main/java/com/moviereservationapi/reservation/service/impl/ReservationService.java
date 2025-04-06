package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.dto.feign.SeatDto;
import com.moviereservationapi.reservation.dto.feign.ShowtimeDto;
import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV1;
import com.moviereservationapi.reservation.dto.reservation.ReservationCreateDto;
import com.moviereservationapi.reservation.dto.reservation.ReservationResponseDto;
import com.moviereservationapi.reservation.exception.SeatAlreadyReservedException;
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
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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
    @Transactional
    public ReservationResponseDto addReservation(@Valid ReservationCreateDto reservationCreateDto) {
        Long userId = reservationCreateDto.getUserId();
        Long showtimeId = reservationCreateDto.getShowtimeId();
        List<Long> seatIds = reservationCreateDto.getSeatIds();

        boolean alreadyReserved = reservationSeatRepository
                .existsBySeatIdInAndReservation_ShowtimeId(seatIds, showtimeId);
        if (alreadyReserved) {
            throw new SeatAlreadyReservedException("Some seats are already taken.");
        }

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

        user.getReservations().add(reservation);

        return ReservationMapper.toReservationResponseDto(reservation, showtimeDto, seatDtos);
    }

    @Override
    public CompletableFuture<ReservationDetailsDtoV1> getReservation(Long reservationId) {
        return null;
    }

    @Override
    public void deleteReservation(Long reservationId) {

    }

    @Override
    public CompletableFuture<Page<ReservationDetailsDtoV1>> getUserReservations(int pageNum, int pageSize, Long userId) {
        return null;
    }
}
