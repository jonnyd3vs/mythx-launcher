package com.mythx.wrapper.handler;

import com.mythx.wrapper.controller.ErrorController;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "GLOBAL")
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Thread: {} threw exception: {}", t.getName(), e.getMessage(), e);

        // Send error to API asynchronously to avoid blocking
        ErrorController.sendErrorAsync("unknown", e);
    }
}
