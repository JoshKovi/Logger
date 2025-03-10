package com.kovisoft.logger.exports;

import com.kovisoft.logger.config.LoggerConfig;
import com.kovisoft.logger.loggerImpl.LogManagerImpl;
import com.kovisoft.logger.loggerImpl.LoggerImpl;

public class LoggerFactory {

    public static LogManager getLogManager(){
        return LogManagerImpl.getInstance();
    }

    public static Logger createLogger(String outputDir, String shortName){
        LogManager lm = getLogManager();
        if(lm.doesLoggerExist(shortName)) return lm.getLogger(shortName);

        LoggerConfig loggerConfig = new LoggerConfig(outputDir, shortName);
        Logger logger = new LoggerImpl(loggerConfig);
        return lm.addLogger(logger);
    }

    public static Logger createLogger(String outputDir, String shortName, int daysToLog){
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
