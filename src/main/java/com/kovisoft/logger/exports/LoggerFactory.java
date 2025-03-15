package com.kovisoft.logger.exports;

import com.kovisoft.logger.config.LoggerConfig;
import com.kovisoft.logger.loggerImpl.LogManagerImpl;
import com.kovisoft.logger.loggerImpl.LoggerImpl;

import java.io.IOException;

public class LoggerFactory {

    public static LogManager getLogManager(){
        return LogManagerImpl.getInstance();
    }

    public static void shutdownManager(){
        getLogManager().stopRunning();
    }

    public static Logger createLogger(String outputDir, String shortName) throws IOException {
        LogManager lm = getLogManager();
        if(lm.doesLoggerExist(shortName)) return lm.getLogger(shortName);

        LoggerConfig loggerConfig = new LoggerConfig(outputDir, shortName);
        Logger logger = new LoggerImpl(loggerConfig);
        return lm.addLogger(logger);
    }

    public static Logger createLogger(String outputDir, String shortName, int daysToLog) throws IOException {
        LogManager lm = getLogManager();
        if(lm.doesLoggerExist(shortName)) return lm.getLogger(shortName);

        LoggerConfig loggerConfig = new LoggerConfig(outputDir, shortName, daysToLog);
        Logger logger = new LoggerImpl(loggerConfig);
        return lm.addLogger(logger);
    }

    public static Logger getLogger(String shortName){
        return getLogManager().getLogger(shortName);
    }

    public static Logger getLoggerByPath(String logPath){
        return getLogManager().getLoggerByPath(logPath);
    }

}

