package com.mythx.launcher.handler;


import com.mythx.launcher.web.error.ErrorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Thread: {} \n threw exception: {}", t.getName(), e.getMessage(), e);
    }
}
