package com.kovisoft.logger.loggerImpl;

import com.kovisoft.logger.exports.LogMethods;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import static com.kovisoft.logger.loggerImpl.LoggerImpl.*;
abstract class LoggerMethods implements LogMethods {

    protected BlockingQueue<String> queue;

    LoggerMethods(BlockingQueue<String> queue){
        this.queue = queue;
    }

    public abstract void log(String logMessage);
    public abstract void log(String logMessage, Exception e);
    protected void log(String type, String logMessage){
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        queue.add(String.format(LOG, timeStamp.toLocalTime().toString(),
                timeStamp.toLocalDate().toString(), type, logMessage));
    }

    protected void log(String type, String logMessage, Exception e){
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        queue.add(String.format(EXCEPTION, timeStamp.toLocalTime().toString(),
                timeStamp.toLocalDate().toString(), type, logMessage, e.getMessage(),
                getStackTraceAsString(e)));
    }

    private String getStackTraceAsString(Exception e){
        return Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining(LINE_DELIMITER));
    }
}