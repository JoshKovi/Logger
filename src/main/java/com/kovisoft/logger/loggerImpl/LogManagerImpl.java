package com.kovisoft.logger.loggerImpl;

import com.kovisoft.logger.config.LoggerConfig;
import com.kovisoft.logger.exports.LogManager;
import com.kovisoft.logger.exports.Logger;

import java.nio.file.Paths;
import java.util.HashMap;

public class LogManagerImpl implements LogManager {

    protected final HashMap<String, Logger> activeLoggers;
    protected static LogManagerImpl lm;
    public static final String LOGGER_NAME = "logger";
    private final Logger loggerLogger; // Must never be its own logger, the universe may end.

    public static LogManager getInstance(){
        if(lm == null){
            lm = new LogManagerImpl();
        }
        return lm;
    }

    public static LogManager getInstance(String overideLogDirPath){
        if(lm == null){
            lm = new LogManagerImpl();
            lm.getLoggerByPath(overideLogDirPath);
        }
        return lm;
    }

    private LogManagerImpl(){
        this(System.getProperty("user.dir") + "/logs/" );
    }
    private LogManagerImpl(String logFullPath){
        activeLoggers = new HashMap<>();
        loggerLogger = getLoggerByPath(Paths.get(logFullPath, LOGGER_NAME).toString());
    }

    @Override
    public boolean doesLoggerExist(String shortName){
        return activeLoggers.containsKey(shortName);
    }

    @Override
    public boolean doesLoggerPathExist(String logPath) {
        return doesLoggerExist(logPath);
    }

    /**
     * Cannot initialize new Logger if none exists, can initialize one
     * if the existing logger has expired.
     * @param shortName The short name (file name without extension)
     * @return The existing logger or the refreshed Logger.
     */
    public Logger getLogger(String shortName){
        if(shortName.endsWith(".log")) shortName = shortName.substring(0, shortName.length()-4);
        Logger logger = activeLoggers.get(shortName);
        if(logger == null){ return null;}
        if(logger.needNewLog()){
            LoggerConfig config = new LoggerConfig(logger.getFile().getParent(), shortName, logger.getDaysToLog());
            return replaceLogger(logger, config);
        } else {
            return logger;
        }
    }

    /**
     * Gets a logger or generates one if there is not one currently.
     * @param logPath The full path to the logger without ".log". (Date not necessary,
     *              it is handled internally.)
     * @return Logger from path or a new instance if it was null or expired.
     */
    @Override
    public Logger getLoggerByPath(String logPath) {
        if(logPath.endsWith(".log")) logPath = logPath.substring(0, logPath.length()-4);
        Logger logger = activeLoggers.get(logPath);
        if(logger == null){
            LoggerConfig config = new LoggerConfig(logPath);
            logger = new LoggerImpl(config);
            return addLogger(logger);
        } else if(logger.needNewLog()){
            LoggerConfig config = new LoggerConfig(logPath, logger.getDaysToLog());
            return replaceLogger(logger, config);
        } else {
            return logger;
        }
    }

    private Logger replaceLogger(Logger logger, LoggerConfig config){
        deleteLogger(logger);
        logger = new LoggerImpl(config);
        logger.setLogger(loggerLogger);
        return addLogger(logger);
    }

    @Override
    public Logger addLogger(Logger logger) {
        String directory = logger.getFile().getParent() + "/" + logger.getShortName();
        activeLoggers.put(directory, logger);
        activeLoggers.put(logger.getShortName(), logger);
        logger.setLogger(loggerLogger);
        return logger;
    }

    @Override
    public void deleteLogger(Logger logger) {
        String directory = logger.getFile().getParent() + "/" + logger.getShortName();
        activeLoggers.remove(directory);
        activeLoggers.remove(directory + ".log"); //doesn't hurt to check it.
        activeLoggers.remove(logger.getShortName());
        logger.stopRunning();
    }

    @Override
    public void deleteLoggerByName(String shortName) {
        deleteLogger(activeLoggers.get(shortName));
    }

    @Override
    public void deleteLoggerByPath(String logPath) {
        deleteLogger(activeLoggers.get(logPath));
    }

}
