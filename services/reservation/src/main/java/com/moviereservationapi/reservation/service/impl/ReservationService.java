package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.dto.feign.SeatDto;
import com.moviereservationapi.reservation.dto.feign.ShowtimeDto;
import com.moviereservationapi.reservation.dto.feign.StripeResponse;
import com.moviereservationapi.reservation.dto.reservation.*;
import com.moviereservationapi.reservation.exception.*;
import com.moviereservationapi.reservation.feign.CinemaClient;
import com.moviereservationapi.reservation.feign.PaymentClient;
import com.moviereservationapi.reservation.feign.ShowtimeClient;
import com.moviereservationapi.reservation.feign.UserClient;
import com.moviereservationapi.reservation.mapper.ReservationMapper;
import com.moviereservationapi.reservation.model.Reservation;
import com.moviereservationapi.reservation.model.ReservationSeat;
import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.repository.ReservationSeatRepository;
import com.moviereservationapi.reservation.service.ICacheService;
import com.moviereservationapi.reservation.service.IJwtService;
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
    private final ShowtimeClient showtimeClient;
    private final CinemaClient cinemaClient;
    private final UserClient userClient;
    private final PaymentClient paymentClient;
    private final RedissonClient redissonClient;
    private final CacheManager cacheManager;
    private final ICacheService cacheService;
    private final TransactionTemplate transactionTemplate;
    private final IJwtService jwtService;

    @Override
    @Transactional
    public ReservationResponseDto addReservationAndCreateCheckoutUrl(@Valid ReservationCreateDto reservationCreateDto) {
        String LOG_PREFIX = "addReservation";

        String userId = jwtService.getLoggedInUserIdFromJwt();
        Long showtimeId = reservationCreateDto.getShowtimeId();
        List<Long> seatIds = reservationCreateDto.getSeatIds();
        if (seatIds.isEmpty()) {
            log.error("{} :: Seat list is empty. userId={}, showtimeId={}", LOG_PREFIX, userId, showtimeId);
            throw new SeatListEmptyException("You must reserve at least one seat.");
        }

        if (seatIds.size() > 6) {
            log.error("{} :: Seat list size > 6. userId={}, showtimeId={}", LOG_PREFIX, userId, showtimeId);
            throw new SeatLimitExceededException("Max seat limit exceeded (6).");
        }

        boolean alreadyReserved = reservationSeatRepository
                .existsBySeatIdInAndReservation_ShowtimeId(seatIds, showtimeId);
        if (alreadyReserved) {
            log.warn("{} :: Seat(s) already reserved. userId={}, showtimeId={}, seatIds={}",
                    LOG_PREFIX, userId, showtimeId, seatIds);
            throw new SeatAlreadyReservedException("Some seats are already taken.");
        }

        ShowtimeDto showtimeDto = showtimeClient.getShowtime(showtimeId);
        List<SeatDto> seatDtos = cinemaClient.getSeats(seatIds);

        boolean invalidSeats = seatDtos.stream()
                .anyMatch(seat -> !seat.getRoomId().equals(showtimeDto.getRoomId()));

        if (invalidSeats) {
            log.error("{} :: One or more seat(s) do not belong to the room of the showtime. showtimeId={}, seatIds={}",
                    LOG_PREFIX, showtimeId, seatIds);
            throw new InvalidSeatRoomException("Selected seats do not belong to the correct room.");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setShowtimeId(showtimeId);
        reservation.setPaymentStatus(PENDING);

        List<ReservationSeat> reservationSeats = seatIds.stream()
                .map(seatId -> ReservationSeat.builder()
                        .seatId(seatId)
                        .reservation(reservation)
                        .build())
                .collect(Collectors.toList());
        reservation.setReservationSeats(reservationSeats);

        Reservation newReservation = reservationRepository.save(reservation);
        userClient.addReservationToUser(reservation.getId());

        StripeResponse stripeResponse = paymentClient.checkout(
                reservationCreateDto.getCurrency(),
                ReservationPayment.builder()
                        .reservationId(newReservation.getId())
                        .seatIds(seatIds)
                        .showtimeId(showtimeId)
                        .userId(userId)
                        .build()

                );

        return ReservationMapper.fromReservationToResponseDto(reservation, showtimeDto, seatDtos, stripeResponse);
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

        log.info("{} :: Evicting caches: 'reservation_user' (all entries) and 'reservation' with key 'reservation_{}'.",
                LOG_PREFIX, reservationId);

        log.info("{} :: Attempting to delete reservation with ID={}.", LOG_PREFIX, reservationId);

        Reservation reservation = findReservationById(reservationId, LOG_PREFIX);

        log.debug("{} :: Reservation found: {}.", LOG_PREFIX, reservation);

        reservationRepository.delete(reservation);

        log.info("{} :: Successfully deleted reservation with ID={}.", LOG_PREFIX, reservationId);
    }

    @Override
    @Async
    public CompletableFuture<Page<ReservationDetailsDtoV2>> getLoggedInUserReservations(int pageNum, int pageSize) {
        String userId = jwtService.getLoggedInUserIdFromJwt();

        String cacheKey = String.format("reservation_user_%s_page_%d_size_%d", userId, pageNum, pageSize);
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
                        log.warn("{} :: No reservations found for userId={}.", LOG_PREFIX, userId);
                        throw new ReservationNotFoundException("There's no reservation found.");
                    }

                    log.info("{} :: Found {} reservation(s) for userId={}. Caching data for key '{}'.",
                            LOG_PREFIX, reservations.getTotalElements(), userId, cacheKey);

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
}
