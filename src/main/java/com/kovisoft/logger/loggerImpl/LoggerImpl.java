package com.kovisoft.logger.loggerImpl;

import com.kovisoft.logger.config.LoggerConfig;
import com.kovisoft.logger.exports.Logger;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggerImpl extends Logger {

    public static final String COLUMN_DELIMITER = "\t;;\t";
    public static final String LINE_DELIMITER = "\t;;;\t";

    protected static final String LOG_HEADER = String.join(COLUMN_DELIMITER,
            List.of("Time","Date","Type","Message","ExceptionMessage","StackTrace\n"));
    protected static final String LOG = String.join(COLUMN_DELIMITER,
            List.of("%s","%s","%s","%s"," "," %n"));
    protected static final String EXCEPTION = String.join(COLUMN_DELIMITER,
            List.of("%s","%s","%s","%s","%s","%s%n"));

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private BufferedWriter bw;

    private final File logFile;
    private final LocalDate date;
    private final String shortName;
    private final int daysToLog;

    private final Thread loggerThread;
    private volatile boolean running = true;
    private Logger logger;

    public LoggerImpl(LoggerConfig config) {
        this.error = new Error(queue);
        this.except = new Except(queue);
        this.log = new Log(queue);
        this.info = new Info(queue);
        this.date = config.getDate();
        this.daysToLog = config.getDaysToLog();
        this.shortName = config.getShortName();
        logFile = config.getLogFile();
        loggerThread = new Thread(() ->{
            try{
                this.openWrite();
                while(running || !queue.isEmpty()){
                    String message = queue.poll();
                    if(message != null){
                        writeLog(message);
                    } else {
                        Thread.sleep(10);
                    }
                }
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
                if(logger == null){
                    System.out.println("Logger thread interrupted. " + logFile.getName());
                } else {
                    logger.except("Logger thread interrupted. " + logFile.getName());
                }
            }
        });
        loggerThread.start();
    }

    private void writeLog(String message){
        try{
            bw.write(message);
        } catch (IOException e) {
            if(logger == null){
                System.out.println("Failed to write to output!" + e.getMessage());
            } else {
                logger.except("Failed to write to output!", e);
            }
            throw new RuntimeException(e);
        }
    }

    private void openWrite() throws IOException {
        bw = new BufferedWriter(new FileWriter(logFile, true));
        if(Files.size(logFile.toPath()) == 0){
            bw.write(LOG_HEADER);
        }
    }

    public void stopRunning(){
        running = false;
        try{
            loggerThread.join();
            bw.close();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            if(logger == null){
                System.out.println("Main thread interrupted while waiting for logger thread to finish." + e.getMessage());
            } else {
                logger.except("Main thread interrupted while waiting for logger thread to finish.", e);
            }
        } catch (IOException e) {
            if(logger == null){
                System.out.println("Error occured while closing buffered writer!" + e.getMessage());
            } else {
                logger.except("Error occured while closing buffered writer!", e);
            }
        }
    }

    public void setLogger(Logger logger){
        this.logger = logger;
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


    private static class Error extends LoggerMethods{
        private static final String TYPE = "Error";
        Error(BlockingQueue<String> queue) {super(queue);}
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
        Log(BlockingQueue<String> queue) {super(queue);}
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
        Except(BlockingQueue<String> queue) {super(queue);}
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
        Info(BlockingQueue<String> queue) {super(queue);}
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
