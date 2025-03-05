package com.kovisoft.logger;

import java.util.HashMap;

public class LogManager {

    private final HashMap<String, Logger> activeLoggers;
    private static LogManager lm;

    protected static LogManager getInstance(){
        if(lm == null){
            lm = new LogManager();
        }
        return lm;
    }

    private LogManager(){
        activeLoggers = new HashMap<>();
    }

    protected boolean doesLoggerExist(String lp){
        return activeLoggers.containsKey(lp);
    }

    protected boolean doesShortNameExist(String shortName){
        return activeLoggers.containsKey(shortName);
    }

    protected Logger getLogger(String lp){
        return activeLoggers.get(lp);
    }

    protected Logger getShortName(String shortName){
        return activeLoggers.get(shortName);
    }

    protected Logger addLogger(String lp, Logger logger, String shortName){
        activeLoggers.put(lp, logger);
        activeLoggers.put(shortName, logger);
        return logger;
    }

    protected void deleteLogger(String lp, String shortName){
        activeLoggers.remove(lp);
        activeLoggers.remove(shortName);
    }
}
