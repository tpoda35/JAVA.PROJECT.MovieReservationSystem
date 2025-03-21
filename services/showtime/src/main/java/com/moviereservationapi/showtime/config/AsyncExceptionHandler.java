package com.moviereservationapi.showtime.config;

import com.moviereservationapi.showtime.exception.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final GlobalExceptionHandler exceptionHandler;

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        exceptionHandler.handleUncaughtExceptions((Exception) ex);
    }
}
