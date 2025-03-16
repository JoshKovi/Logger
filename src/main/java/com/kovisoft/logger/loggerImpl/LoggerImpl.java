package com.kovisoft.logger.loggerImpl;

import com.kovisoft.logger.config.LoggerConfig;
import com.kovisoft.logger.exports.Logger;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;

public class LoggerImpl extends Logger implements AutoCloseable {

    public static final String COLUMN_DELIMITER = "\t;;\t";
    public static final String LINE_DELIMITER = "\t;;;\t";

    protected static final String LOG_HEADER = String.join(COLUMN_DELIMITER,
            List.of("Time","Date","Type","Message","ExceptionMessage","StackTrace"));
    protected static final String LOG = String.join(COLUMN_DELIMITER,
            List.of("%s","%s","%s","%s","",""));
    protected static final String EXCEPTION = String.join(COLUMN_DELIMITER,
            List.of("%s","%s","%s","%s","%s","%s"));

    protected final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    private final File logFile;
    private final LocalDate date;
    private final String shortName;
    private final int daysToLog;

    private final ScheduledExecutorService loggerService;
    private volatile boolean writingComplete = true;
    private final BufferedWriter bw;

    public LoggerImpl(LoggerConfig config) throws IOException {
        this.date = config.getDate();
        this.daysToLog = config.getDaysToLog();
        this.shortName = config.getShortName();
        logFile = config.getLogFile();
        bw = new BufferedWriter(new FileWriter(logFile, true));
        if(isEmptyFile()){
            bw.write(LOG_HEADER);
            bw.newLine();
        }
        loggerService = Executors.newScheduledThreadPool(1);
        loggerService.scheduleWithFixedDelay(this::writeLogs, 5, 5, TimeUnit.MILLISECONDS);
        this.error = new Error(this);
        this.except = new Except(this);
        this.log = new Log(this);
        this.info = new Info(this);
        this.warn = new Warn(this);
    }


    private void writeLogs(){
        writingComplete = false;
        while(!queue.isEmpty()){
            try{
                String message = queue.poll(1, TimeUnit.MILLISECONDS);
                if(message != null) {
                    bw.write(message);
                    bw.newLine();
                }
                bw.flush();
            } catch (IOException | InterruptedException e) {
                System.out.println("Failed to write to output!" + e.getMessage());
            }
        }

        writingComplete = true;
    }

    private boolean isEmptyFile() throws IOException {
        return Files.size(logFile.toPath()) == 0;
    }


    @Override
    public boolean safeToClose(){
        return queue.isEmpty() && writingComplete;
    }

    @Override
    public boolean stopRunning() throws Exception {
        try {
            while (!safeToClose()) {
                Thread.onSpinWait();
                writeLogs();
            }
            bw.flush();
            loggerService.shutdown();
            return loggerService.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread interrupted during shutdown.");
        } finally {
            close();
        }
        return false;
    }

    protected void addToQueue(String logMessage){
        try{
            queue.put(logMessage);
        } catch (InterruptedException e) {
            System.out.println("Interrupt Exception when attempting to put log: " + logMessage);
        }

    }

    @Override
    public boolean needNewLog() {
        return date.plusDays(daysToLog).isBefore(LocalDate.now());
    }

    @Override
    public String getShortName(){
        return shortName;
    }

    @Override
    public int getDaysToLog(){
        return daysToLog;
    }

    @Override
    public File getFile(){
        return logFile;
    }

    @Override
    public String getLineDelimiter() {
        return LINE_DELIMITER;
    }

    @Override
    public String getColumnDelimiter() {
        return COLUMN_DELIMITER;
    }

    @Override
    public void error(String logMessage) {
        error.log(logMessage);
    }

    @Override
    public void error(String logMessage, Exception e) {
        error.log(logMessage, e);
    }

    @Override
    public void except(String logMessage) {
        except.log(logMessage);
    }

    @Override
    public void except(String logMessage, Exception e) {
        except.log(logMessage, e);
    }

    @Override
    public void warn(String logMessage) {
        warn.log(logMessage);
    }

    @Override
    public void warn(String logMessage, Exception e) {
        warn.log(logMessage, e);
    }

    @Override
    public void log(String logMessage) {
        log.log(logMessage);
    }

    @Override
    public void log(String logMessage, Exception e) {
        log.log(logMessage, e);
    }

    @Override
    public void info(String logMessage) {
        info.log(logMessage);
    }

    @Override
    public void info(String logMessage, Exception e) {
        info.log(logMessage, e);
    }

    @Override
    public void close() throws Exception{
        Exception lastException = null;
        try{
            loggerService.close();
        } catch (Exception e){
            System.out.println("Exception thrown while attempting to close service executor. " + e.getMessage());
            lastException = e;
        }
        try{
            bw.flush();
            bw.close();
        } catch (Exception e){
            System.out.println("Exception thrown while attempting to close buffered writer. " + e.getMessage());
            lastException = e;
        }
        writingComplete = false;
        if(lastException != null) throw lastException;
    }

    private static class Warn extends LoggerMethods{
        private static final String TYPE = "Warn";
        Warn(LoggerImpl logger) {super(logger);}
        @Override
        public void log(String logMessage) {
            this.log(TYPE, logMessage);
        }
        @Override
        public void log(String logMessage, Exception e) {
            this.log(TYPE, logMessage, e);
        }
    }

    private static class Error extends LoggerMethods{
        private static final String TYPE = "Error";
        Error(LoggerImpl logger) {super(logger);}
        @Override
        public void log(String logMessage) {
            this.log(TYPE, logMessage);
        }
        @Override
        public void log(String logMessage, Exception e) {
            this.log(TYPE, logMessage, e);
        }
    }

    private static class Log extends LoggerMethods{
        private static final String TYPE = "Log";
        Log(LoggerImpl logger) {super(logger);}
        @Override
        public void log(String logMessage) {
            this.log(TYPE, logMessage);
        }
        @Override
        public void log(String logMessage, Exception e) {
            this.log(TYPE, logMessage, e);
        }
    }
    private static class Except extends LoggerMethods{
        private static final String TYPE = "Exception";
        Except(LoggerImpl logger) {super(logger);}
        @Override
        public void log(String logMessage) {
            this.log(TYPE, logMessage);
        }
        @Override
        public void log(String logMessage, Exception e) {
            this.log(TYPE, logMessage, e);
        }
    }
    private static class Info extends LoggerMethods{
        private static final String TYPE = "Info";
        Info(LoggerImpl logger) {super(logger);}
        @Override
        public void log(String logMessage) {
            this.log(TYPE, logMessage);
        }
        @Override
        public void log(String logMessage, Exception e) {
            this.log(TYPE, logMessage, e);
        }
    }
}
