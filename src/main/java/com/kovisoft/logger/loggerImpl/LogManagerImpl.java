package com.kovisoft.logger.loggerImpl;

import com.kovisoft.logger.config.LoggerConfig;
import com.kovisoft.logger.exports.LogManager;
import com.kovisoft.logger.exports.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LogManagerImpl implements LogManager, AutoCloseable {

    protected final HashMap<String, Logger> activeLoggers;
    protected static LogManagerImpl lm;

    public static LogManager getInstance(){
        if(lm == null){
            lm = new LogManagerImpl();
        }
        return lm;
    }

    private LogManagerImpl(){
        activeLoggers = new HashMap<>();
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
    public Logger getLogger(String shortName) {
        if(shortName.endsWith(".log")) shortName = shortName.substring(0, shortName.length()-4);
        Logger logger = activeLoggers.get(shortName);
        if(logger == null){ return null;}
        if(logger.needNewLog()){
            LoggerConfig config = new LoggerConfig(logger.getFile().getParent(), shortName, logger.getDaysToLog());
            return tryReplaceLogger(logger, config);
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
        if(logger == null){ return null;}
        if(logger.needNewLog()){
            LoggerConfig config = new LoggerConfig(logPath, logger.getDaysToLog());
            return tryReplaceLogger(logger, config);
        } else {
            return logger;
        }
    }

    private Logger tryReplaceLogger(Logger logger, LoggerConfig config) {
        try{
            logger = new LoggerImpl(config);
        } catch (IOException e) {
            System.out.println("Unable to instantiate new log this time, keep using old for now. " + e.getMessage());
        }
        removeLogger(logger);
        return addLogger(logger);
    }

    @Override
    public Logger addLogger(Logger logger) {
        String directory = logger.getFile().getParent() + "/" + logger.getShortName();
        activeLoggers.put(directory, logger);
        activeLoggers.put(logger.getShortName(), logger);
        return logger;
    }

    @Override
    public void removeLogger(Logger logger) {
        String directory = logger.getFile().getParent() + "/" + logger.getShortName();
        activeLoggers.remove(directory);
        activeLoggers.remove(directory + ".log"); //doesn't hurt to check it.
        activeLoggers.remove(logger.getShortName());
        try{
            logger.stopRunning();
        } catch (Exception e){
            System.out.printf("Logger %s could not be gracefully shutdown.%n", logger.getShortName());
            logger = null;
        }

    }

    @Override
    public void removeLoggerByName(String shortName) {
        removeLogger(activeLoggers.get(shortName));
    }

    @Override
    public void removeLoggerByPath(String logPath) {
        removeLogger(activeLoggers.get(logPath));
    }

    @Override
    public void stopRunning(){
        for(Map.Entry<String, Logger> lEntry : activeLoggers.entrySet()){
            try{
                lEntry.getValue().stopRunning();
            } catch (Exception e){
                System.out.println("Exception thrown while attempting to stop logger." + e.getMessage());
                lEntry = null;
            }
        }
    }

    @Override
    public void close() {
        for(Map.Entry<String, Logger> lEntry : activeLoggers.entrySet()){
            try{
                ((LoggerImpl)lEntry.getValue()).close();
            } catch (Exception e){
                System.out.println("Exception thrown while attempting to close logger." + e.getMessage());
            }
        }
    }
}
