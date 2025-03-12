package com.kovisoft.logger.exports;

public abstract class Logger implements LoggerInterface {
    public LogMethods error;
    public LogMethods log;
    public LogMethods info;
    public LogMethods except;
    public LogMethods warn;

}
