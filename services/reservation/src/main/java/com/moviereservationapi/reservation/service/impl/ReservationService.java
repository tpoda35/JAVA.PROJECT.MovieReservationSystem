package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.dto.feign.SeatDto;
import com.moviereservationapi.reservation.dto.feign.ShowtimeDto;
import com.moviereservationapi.reservation.dto.reservation.ReservationCreateDto;
import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV1;
import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV2;
import com.moviereservationapi.reservation.dto.reservation.ReservationResponseDto;
import com.moviereservationapi.reservation.exception.*;
import com.moviereservationapi.reservation.feign.CinemaClient;
import com.moviereservationapi.reservation.feign.ShowtimeClient;
import com.moviereservationapi.reservation.mapper.ReservationMapper;
import com.moviereservationapi.reservation.model.Reservation;
import com.moviereservationapi.reservation.model.ReservationSeat;
import com.moviereservationapi.reservation.model.User;
import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.repository.ReservationSeatRepository;
import com.moviereservationapi.reservation.repository.UserRepository;
import com.moviereservationapi.reservation.service.ICacheService;
import com.moviereservationapi.reservation.service.IReservationService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
    private final RedissonClient redissonClient;
    private final CacheManager cacheManager;
    private final ICacheService cacheService;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Transactional
    public ReservationResponseDto addReservation(@Valid ReservationCreateDto reservationCreateDto) {
        String LOG_PREFIX = "addReservation";

        Long userId = reservationCreateDto.getUserId();
        Long showtimeId = reservationCreateDto.getShowtimeId();
        List<Long> seatIds = reservationCreateDto.getSeatIds();
        if (seatIds.isEmpty()) {
            log.info("{} :: Seat list empty.", LOG_PREFIX);
            throw new SeatListEmtpyException("You must reserve at least one seat.");
        }

        boolean alreadyReserved = reservationSeatRepository
                .existsBySeatIdInAndReservation_ShowtimeId(seatIds, showtimeId);
        if (alreadyReserved) {
            log.info("{} :: Seat(s) already reserved.", LOG_PREFIX);
            throw new SeatAlreadyReservedException("Some seats are already taken.");
        }

        User user = findUserById(userId, LOG_PREFIX);

        ShowtimeDto showtimeDto = showtimeClient.getShowtime(showtimeId);
        List<SeatDto> seatDtos = cinemaClient.getSeats(seatIds);

        boolean invalidSeats = seatDtos.stream()
                .anyMatch(seat -> !seat.getRoomId().equals(showtimeDto.getRoomId()));

        if (invalidSeats) {
            log.info("{} :: One or more seat(s) do not belong to the room of the showtime.", LOG_PREFIX);
            throw new InvalidSeatRoomException("Selected seats do not belong to the correct room.");
        }

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
    @Async
    public CompletableFuture<ReservationDetailsDtoV1> getReservation(Long reservationId) {
        String cacheKey = String.format("reservation_%d", reservationId);
        Cache cache = cacheManager.getCache("reservation");
        String LOG_PREFIX = "getReservation";

        ReservationDetailsDtoV1 reservationDetailsDtoV1 =
                cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, ReservationDetailsDtoV1.class);
        if (reservationDetailsDtoV1 != null) {
            return CompletableFuture.completedFuture(reservationDetailsDtoV1);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            // The thread will wait for 10 sec max, 30 sec ttl/key.
            // Change lock to env variables.
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                reservationDetailsDtoV1 =
                        cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, ReservationDetailsDtoV1.class);
                if (reservationDetailsDtoV1 != null) {
                    return CompletableFuture.completedFuture(reservationDetailsDtoV1);
                }

                List<Long> seatIds = reservationSeatRepository.findSeatIdsByReservationId(reservationId);
                List<SeatDto> seatDtos = cinemaClient.getSeats(seatIds);
                Reservation reservation = findReservationById(reservationId, LOG_PREFIX);

                reservationDetailsDtoV1 = ReservationMapper.fromReservationToDetailsDtoV1(reservation, seatDtos);

                cacheService.saveInCache(cache, cacheKey, reservationDetailsDtoV1, LOG_PREFIX);

                return CompletableFuture.completedFuture(reservationDetailsDtoV1);
            } else {
                failedAcquireLock(LOG_PREFIX, cacheKey);
                throw new LockAcquisitionException("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockInterruptedException("Thread interrupted while acquiring lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "reservation",
                            key = "'reservation_' + #reservationId"
                    ),
                    @CacheEvict(
                            value = "reservation_user",
                            allEntries = true
                    )
            }
    )
    public void deleteReservation(Long reservationId) {
        String LOG_PREFIX = "deleteReservation";

        log.info("{} :: Evicting cache 'reservation_user' and 'reservation' with the key of 'reservation_{}'", LOG_PREFIX, reservationId);
        log.info("{} :: Deleting reservation with the id of {}.", LOG_PREFIX, reservationId);

        Reservation reservation = findReservationById(reservationId, LOG_PREFIX);
        log.info("{} :: Reservation found with the id of {} and data of {}.", LOG_PREFIX, reservationId, reservation);

        reservationRepository.delete(reservation);
    }

    @Override
    @Async
    public CompletableFuture<Page<ReservationDetailsDtoV2>> getUserReservations(int pageNum, int pageSize, Long userId) {
        String cacheKey = String.format("reservation_user_%d_page_%d_size_%d", userId, pageNum, pageSize);
        Cache cache = cacheManager.getCache("reservation_user");
        String LOG_PREFIX = "getUserReservations";

        Page<ReservationDetailsDtoV2> reservationDetailsDtoV2s =
                cacheService.getCachedUserReservationsPage(cache, cacheKey, LOG_PREFIX);
        if (reservationDetailsDtoV2s != null) {
            return CompletableFuture.completedFuture(reservationDetailsDtoV2s);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            // The thread will wait for 10 sec max, 30 sec ttl/key.
            // Change lock to env variables.
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                reservationDetailsDtoV2s = cacheService.getCachedUserReservationsPage(cache, cacheKey, LOG_PREFIX);
                if (reservationDetailsDtoV2s != null) {
                    return CompletableFuture.completedFuture(reservationDetailsDtoV2s);
                }

                reservationDetailsDtoV2s = transactionTemplate.execute(status -> {
                    Page<Reservation> reservations =
                            reservationRepository.findByUserId(userId, PageRequest.of(pageNum, pageSize));
                    if (reservations.isEmpty()) {
                        log.info("{} :: No reservation found.", LOG_PREFIX);
                        throw new ReservationNotFoundException("There's no reservation found.");
                    }

                    log.info("{} :: Found {} reservation. Caching data for key '{}'.",
                            LOG_PREFIX, reservations.getTotalElements(), cacheKey);

                    return reservations.map(ReservationMapper::fromReservationToDetailsDtoV2);
                });

                cacheService.saveInCache(cache, cacheKey, reservationDetailsDtoV2s, LOG_PREFIX);

                return CompletableFuture.completedFuture(reservationDetailsDtoV2s);
            } else {
                failedAcquireLock(LOG_PREFIX, cacheKey);
                throw new LockAcquisitionException("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockInterruptedException("Thread interrupted while acquiring lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void failedAcquireLock(String LOG_PREFIX, String cacheKey) {
        log.warn("{} :: Failed to acquire lock for key: {}", LOG_PREFIX, cacheKey);
    }

    private Reservation findReservationById(Long reservationId, String LOG_PREFIX) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("{} :: Reservation not found with id: {}", LOG_PREFIX, reservationId);
                    return new ReservationNotFoundException("Reservation not found with ID: " + reservationId);
                });
    }

    private User findUserById(Long userId, String LOG_PREFIX) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("{} :: User not found with id: {}", LOG_PREFIX, userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
    }
}
