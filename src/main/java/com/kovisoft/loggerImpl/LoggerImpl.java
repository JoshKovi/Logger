package com.kovisoft.loggerImpl;

import com.kovisoft.config.LoggerConfig;
import com.kovisoft.logger.LogMethods;
import com.kovisoft.logger.Logger;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class LoggerImpl implements Logger {

    private abstract class LoggerMethods implements LogMethods {
        public abstract void log(String logMessage);
        public abstract void log(String logMessage, Exception e);
        protected void log(String type, String logMessage){
            LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
            queue.add(String.format(LOG, timeStamp.toLocalTime().toString(),
                    timeStamp.toLocalDate().toString(), type, logMessage));
        }

        protected void log(String type, String logMessage, Exception e){
            LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
            queue.add(String.format(EXCEPTION, timeStamp.toLocalTime().toString(),
                    timeStamp.toLocalDate().toString(), type, logMessage, e.getMessage(),
                    getStackTraceAsString(e)));
        }

        private String getStackTraceAsString(Exception e){
            return Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\t\t;;;\t\t"));
        }
    }

    private static final String LOG_HEADER = "Time\t|\tDate\t|\tType\t|\tMessage\t|\tExceptionMessage\t|\tStackTrace\n";
    private static final String LOG = "%s\t|\t%s\t|\t%s\t|\t%s\t|\t \t|\t %n";
    private static final String EXCEPTION = "%s\t|\t%s\t|\t%s\t|\t%s\t|\t%s\t|\t%s%n";

    private final File logFile;
    private final String shortName;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final Thread loggerThread;
    private volatile boolean running = true;

    private final Error error = new Error();
    private final Log log = new Log();
    private final Except exception = new Except();
    private final Info info = new Info();

    public LoggerImpl(LoggerConfig config, String shortName){
        logFile = config.getLogFile();
        this.shortName = shortName;
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
        }
    }

    private boolean isEmptyFile() throws IOException {
        return Files.size(logFile.toPath()) == 0;
    }

    public void stopRunning(){
        running = false;
        loggerThread.interrupt();
        Logger.removeLogger(
                logFile.getAbsolutePath().replaceAll("\\\\", "/"),
                shortName
        );
    }

    @Override
    public LogMethods error() {
        return error;
    }

    @Override
    public LogMethods log() {
        return log;
    }

    @Override
    public LogMethods exception() {
        return exception;
    }

    @Override
    public LogMethods info() {
        return info;
    }


    public class Error extends LoggerMethods{
        private static final String TYPE = "Error";
        @Override
        public void log(String logMessage) {
            this.log(TYPE, logMessage);
        }
        @Override
        public void log(String logMessage, Exception e) {
            this.log(TYPE, logMessage, e);
        }
    }

    public class Log extends LoggerMethods{
        private static final String TYPE = "Log";
        @Override
        public void log(String logMessage) {
            this.log(TYPE, logMessage);
        }
        @Override
        public void log(String logMessage, Exception e) {
            this.log(TYPE, logMessage, e);
        }
    }
    public class Except extends LoggerMethods{
        private static final String TYPE = "Exception";
        @Override
        public void log(String logMessage) {
            this.log(TYPE, logMessage);
        }
        @Override
        public void log(String logMessage, Exception e) {
            this.log(TYPE, logMessage, e);
        }
    }
    public class Info extends LoggerMethods{
        private static final String TYPE = "Info";
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
