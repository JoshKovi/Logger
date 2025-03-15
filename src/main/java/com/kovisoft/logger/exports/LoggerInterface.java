package com.kovisoft.logger.exports;


import java.io.File;

public interface LoggerInterface {


    String getLineDelimiter();
    String getColumnDelimiter();
    void error(String logMessage);
    void error(String logMessage, Exception e);
    void except(String logMessage);
    void except(String logMessage, Exception e);
    void warn(String logMessage);
    void warn(String logMessage, Exception e);
    void log(String logMessage);
    void log(String logMessage, Exception e);
    void info(String logMessage);
    void info(String logMessage, Exception e);
    boolean safeToClose();
    boolean stopRunning() throws Exception;
    boolean needNewLog();
    String getShortName();
    int getDaysToLog();
    File getFile();

}
