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

    private final File logFile;
    private final LocalDate date;
    private final String shortName;
    private final int daysToLog;

    private final Thread loggerThread;
    private volatile boolean running = true;

    public LoggerImpl(LoggerConfig config){
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
                while(running || !queue.isEmpty()){
                    String message = queue.take();
                    writeLog(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Logger thread interrupted. " + logFile.getName());
            }
        });
        loggerThread.start();
    }

    private void writeLog(String message){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))){
            if(isEmptyFile()){
                bw.write(LOG_HEADER);
            }
            String outputMessage = message;
            do{
                bw.write(outputMessage);
                if(!queue.isEmpty()){
                    outputMessage = queue.take();
                }
            } while(!queue.isEmpty());

        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to write to output!");
            throw new RuntimeException(e);
        }
    }

    private boolean isEmptyFile() throws IOException {
        return Files.size(logFile.toPath()) == 0;
    }

    public void stopRunning(){
        running = false;
        loggerThread.interrupt();
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
