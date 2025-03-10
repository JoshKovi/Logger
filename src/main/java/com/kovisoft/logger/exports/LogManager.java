package com.kovisoft.logger.exports;


public interface LogManager {

    /**
     * I strongly recommend you implement this as a singleton class in your project,
     * if you decide to not use the default setup.
     */

    boolean doesLoggerExist(String shortName);
    boolean doesLoggerPathExist(String logPath);
    Logger getLogger(String shortName);
    Logger getLoggerByPath(String logPath);
    Logger addLogger(Logger logger);
    void deleteLogger(Logger logger);
    void deleteLoggerByName(String shortName);
    void deleteLoggerByPath(String logPath);
}
