package com.kovisoft.logger.loggerImpl;

import com.kovisoft.logger.config.LoggerConfig;
import com.kovisoft.logger.exports.Logger;
import com.kovisoft.logger.exports.TestLoggerFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestLoggerImpl {

    static final String userdir = System.getProperty("user.dir");
    static final String outputDir = "test/implLogs";
    static final String SHORT_NAME = "implTest";
    static Logger logger;
    static ArrayList<String> logged = new ArrayList<>();
    static String LOG_HEADER;
    static String LOG;
    static String EXCEPTION;

    @BeforeAll
    public static void setupEnvironment(){
        try{
            TestLoggerFactory.deleteDirectory(Paths.get(userdir, outputDir));
        } catch (IOException e) {
            System.out.println("Could not delete directory!");
        }
        LoggerConfig loggerConfig = new LoggerConfig(userdir + "/" + outputDir, SHORT_NAME);
        Assertions.assertDoesNotThrow(()->{
            logger = new LoggerImpl(loggerConfig);
        });
        try{
            Field field = LoggerImpl.class.getDeclaredField("LOG_HEADER");
            field.setAccessible(true);
            LOG_HEADER = ((String) field.get(null)).replaceAll("\n", "");
            field = LoggerImpl.class.getDeclaredField("LOG");
            field.setAccessible(true);
            LOG = ((String) field.get(null)).replaceAll("\n", "");
            field = LoggerImpl.class.getDeclaredField("EXCEPTION");
            field.setAccessible(true);
            EXCEPTION = ((String) field.get(null)).replaceAll("\n", "");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        logged.add(LOG_HEADER);
    }

    @Test
    @Order(11) // Not an after all because it has some useful tests, maybe split this out latter.
    public void teardownEnvironment(){
        List<String> lines = List.of();
        try{
            boolean awaitCompleted = logger.stopRunning();
            Assertions.assertTrue(awaitCompleted);
            lines = Files.readAllLines(logger.getFile().toPath());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try{
            //Only delete if assertion will pass later, makes debugging way easier.
            if(lines.size() == logged.size()){
                TestLoggerFactory.deleteDirectory(Paths.get(userdir, outputDir));
            }
        } catch (IOException e) {
            System.out.println("Could not delete directory!");
        }
        boolean someNotEqual = false;
        Assertions.assertEquals(logged.size(), lines.size());
        for(int i = 0; i < logged.size(); i++){
            String[] loggedLine = logged.get(i).split(logger.getColumnDelimiter());
            String[] fileLine = lines.get(i).split(logger.getColumnDelimiter());
            Assertions.assertEquals(loggedLine.length, fileLine.length);
            for(int j = 0; j < loggedLine.length; j++){
                if(i > 0 && j == 0) continue; //The timestamp will not be the same and its not worth the effort to check it.
                // TBH this might cause problems later, but they were not exactly easy to debug and the visual looks
                // about the same so ill leave it for now.
                String logItem = loggedLine[j].replaceAll("[\r\n]", "");
                String fileItem = fileLine[j].replaceAll("[\r\n]", "");

                if(!logItem.equals(fileItem)){
                    System.out.printf("Logged[%d][%d]: '%s', fileLine[%d][%d]: '%s'%n",
                            i, j, loggedLine[j], i, j, fileLine[j]);
                    someNotEqual = true;
                }
            }
        }

        Assertions.assertFalse(someNotEqual);


    }

    //Mirror of the LoggerMethods implementation for consistency.
    private static String getStackTraceAsString(Exception e){
        return Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining(logger.getLineDelimiter()));
    }


    @Test
    @Order(1)
    public void test_error(){
        String test = "Logging error with no Exception present";
        Assertions.assertDoesNotThrow(()->logger.error(test));
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        logged.add(String.format(LOG, timeStamp.toLocalTime(), timeStamp.toLocalDate(), "Error", test));
    }

    @Test
    @Order(2)
    public void test_error_e(){
        String test = "Logging error with Exception present";
        Exception e = new Exception("Here is an error Exception!");
        Assertions.assertDoesNotThrow(()->logger.error(test, e));
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        logged.add(String.format(EXCEPTION, timeStamp.toLocalTime(), timeStamp.toLocalDate(), "Error", test,
                e.getMessage(), getStackTraceAsString(e)));
    }

    @Test
    @Order(3)
    public void test_except(){
        String test = "Logging Exception with no Exception present";
        Assertions.assertDoesNotThrow(()->logger.except(test));
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        logged.add(String.format(LOG, timeStamp.toLocalTime(), timeStamp.toLocalDate(), "Exception", test));
    }

    @Test
    @Order(4)
    public void test_except_e(){
        String test = "Logging Exception with Exception present";
        Exception e = new Exception("Here is an Exception Exception!");
        Assertions.assertDoesNotThrow(()->logger.except(test, e));
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        logged.add(String.format(EXCEPTION, timeStamp.toLocalTime(), timeStamp.toLocalDate(), "Exception", test,
                e.getMessage(), getStackTraceAsString(e)));
    }

    @Test
    @Order(5)
    public void test_log(){
        String test = "Logging Log with no Exception present";
        Assertions.assertDoesNotThrow(()->logger.log(test));
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        logged.add(String.format(LOG, timeStamp.toLocalTime(), timeStamp.toLocalDate(), "Log", test));
    }

    @Test
    @Order(6)
    public void test_log_e(){
        String test = "Logging Log with Exception present";
        Exception e = new Exception("Here is a Log Exception!");
        Assertions.assertDoesNotThrow(()->logger.log(test, e));
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        logged.add(String.format(EXCEPTION, timeStamp.toLocalTime(), timeStamp.toLocalDate(), "Log", test,
                e.getMessage(), getStackTraceAsString(e)));
    }

    @Test
    @Order(7)
    public void test_info(){
        String test = "Logging Info with no Exception present";
        Assertions.assertDoesNotThrow(()->logger.info(test));
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        logged.add(String.format(LOG, timeStamp.toLocalTime(), timeStamp.toLocalDate(), "Info", test));
    }

    @Test
    @Order(8)
    public void test_info_e(){
        String test = "Logging Info with Exception present";
        Exception e = new Exception("Here is an Info Exception!");
        Assertions.assertDoesNotThrow(()->logger.info(test, e));
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        logged.add(String.format(EXCEPTION, timeStamp.toLocalTime(), timeStamp.toLocalDate(), "Info", test,
                e.getMessage(), getStackTraceAsString(e)));
    }

    @Test
    @Order(9)
    public void test_warn(){
        String test = "Logging Warn with no Exception present";
        Assertions.assertDoesNotThrow(()->logger.warn(test));
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        logged.add(String.format(LOG, timeStamp.toLocalTime(), timeStamp.toLocalDate(), "Warn", test));
    }

    @Test
    @Order(10)
    public void test_warn_e(){
        String test = "Logging Warn with Exception present";
        Exception e = new Exception("Here is a Warn Exception!");
        Assertions.assertDoesNotThrow(()->logger.warn(test, e));
        LocalDateTime timeStamp = LocalDateTime.now(ZoneId.of("America/New_York"));
        logged.add(String.format(EXCEPTION, timeStamp.toLocalTime(), timeStamp.toLocalDate(), "Warn", test,
                e.getMessage(), getStackTraceAsString(e)));
    }

}
