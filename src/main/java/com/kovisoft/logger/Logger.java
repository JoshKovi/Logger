package com.kovisoft.logger;

import com.kovisoft.config.LoggerConfig;
import com.kovisoft.loggerImpl.LoggerImpl;

public interface Logger {



    LogMethods error();
    LogMethods log();
    LogMethods exception();
    LogMethods info();
    void stopRunning();


    static Logger createLogger(String outputDir, String outputFile, String shortName){
        String logPath = outputDir + "/" + outputFile;
        return createLogger(logPath, shortName);
    }

    static Logger createLogger(String outputPath, String shortName){
        LogManager lm = LogManager.getInstance();
        outputPath += !outputPath.endsWith(".log") ? ".log" : "";
        return lm.doesLoggerExist(outputPath) ? lm.getLogger(outputPath) :
                lm.addLogger(outputPath, new LoggerImpl(new LoggerConfig(outputPath), shortName), shortName);
    }

    static Logger getLogger(String shortName){
        return LogManager.getInstance().getShortName(shortName);
    }

    static Logger getLoggerByPath(String lp){
        return LogManager.getInstance().getLogger(lp);
    }

    static Boolean loggerExists(String shortName){
        return LogManager.getInstance().doesShortNameExist(shortName);
    }

    static void removeLogger(String lp, String shortName){
        LogManager.getInstance().deleteLogger(lp, shortName);
    }


}
