package com.kovisoft.logger.config;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class LoggerConfig {
    public final String OUTPUT_DIRECTORY;
    public final String OUTPUT_FILE;
    private final File logFile;

    private String shortName;
    private int daysToLog = 10;
    private final LocalDate date = LocalDate.now();

    private final static String LOG_CREATION_FAILURE = "Could not generate log file in existing directory.%nLog File: %s,%nDirectory %s%n";
    private final static String LOG_ALREADY_EXIST_NOT_FILE = "Could not generate log file as it exists but is not a file. Log File: %s%n";
    private final static String DIRECTORY_NOT_DIRECTORY = "The input directory cannot be created as it already exists and is not a directory!%nDirectory File: %s%n";
    private final static String DIRECTORIES_NOT_CREATED = "Could not make one or more directories.%nDirectory Path: %s%n";

    public LoggerConfig(String outputDir, String shortName) throws RuntimeException{
        OUTPUT_DIRECTORY = outputDir;
        this.shortName = shortName;
        OUTPUT_FILE = getFileName();
        this.logFile = genFileIfAbsent();
    }
    public LoggerConfig(String outputPath) throws RuntimeException{
        int lastIndex = outputPath.lastIndexOf('/');
        OUTPUT_DIRECTORY = outputPath.substring(0, lastIndex);
        this.shortName = outputPath.substring(lastIndex + 1);
        OUTPUT_FILE = getFileName();
        this.logFile = genFileIfAbsent();
    }

    public LoggerConfig(String outputDir, String outputFile, int daysToLog) throws RuntimeException{
        this(outputDir, outputFile);
        this.daysToLog = daysToLog;
    }

    public LoggerConfig(String outputPath, int daysToLog) throws RuntimeException{
        this(outputPath);
        this.daysToLog = daysToLog;
    }

    private String getFileName(){
        if(shortName.endsWith(".log")) {
            this.shortName = shortName.substring(0, shortName.length() - 4);
        }
        return shortName + date.toString() + ".log";
    }


    private File genFileIfAbsent() throws RuntimeException{
        File directory = new File(OUTPUT_DIRECTORY);
        File log = new File(directory, OUTPUT_FILE);
        try{
            if(log.exists() && log.isFile()) return log;
            if(log.exists()){
                throw new RuntimeException(String.format(LOG_ALREADY_EXIST_NOT_FILE, OUTPUT_FILE));
            }
            if(directory.exists() && directory.isDirectory()){
                if(log.createNewFile()) return log;
                throw new RuntimeException(String.format(LOG_CREATION_FAILURE, OUTPUT_FILE, OUTPUT_DIRECTORY));
            }
            if(directory.exists()){
                throw new RuntimeException(String.format(DIRECTORY_NOT_DIRECTORY, OUTPUT_DIRECTORY));
            }
            if(!directory.mkdirs()){
                throw new RuntimeException(String.format(DIRECTORIES_NOT_CREATED, OUTPUT_DIRECTORY));
            }
            if(!log.createNewFile()){
                throw new RuntimeException(String.format(LOG_CREATION_FAILURE, OUTPUT_FILE, OUTPUT_DIRECTORY));
            }
            return log;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getLogFile(){return logFile;}
    public String getShortName(){return shortName;}
    public int getDaysToLog(){return daysToLog;}
    public LocalDate getDate(){return date;}
}
