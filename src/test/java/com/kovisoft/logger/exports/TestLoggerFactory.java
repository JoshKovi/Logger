package com.kovisoft.logger.exports;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;


public class TestLoggerFactory {

    static final String userdir = System.getProperty("user.dir");
    static final String outputDir = "test/factorlogs";
    static final SecureRandom random = new SecureRandom();

    static final LocalDate date = LocalDate.now();
    static final Path dirPath = Paths.get(String.format("%s/%s", userdir, outputDir));

    @BeforeAll
    public static void setupEnviroment(){
        assertNotNull(userdir);
        System.out.println(userdir);
        try{
            deleteDirectory(dirPath);
        } catch (IOException e) {
            System.out.println("Could not delete directory!");
        }
        Assertions.assertFalse(Files.exists(Paths.get(String.format("%s/%s", userdir, outputDir))));
    }

    @AfterAll
    public static void teardownEnviroment(){
        assertDoesNotThrow(()->{
            deleteDirectory(dirPath);
            LoggerFactory.shutdownManager();
        });

    }

    public static void deleteDirectory(Path path) throws IOException {
        Files.walk(path, 5)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }




    @Test
    public void test_getLogManager(){
        assertDoesNotThrow(() ->{LoggerFactory.getLogManager();});
        LogManager lm = LoggerFactory.getLogManager();
        assertNotNull(lm);
    }

    @Test
    public void test_createLogger_2inputs_shortname() throws IOException {
        String shortName = "log" +random.nextLong(10000, 10000000);
        assertDoesNotThrow(()->{LoggerFactory.createLogger(dirPath.toString(), shortName);});
        Assertions.assertTrue(Files.exists(dirPath));
        Path filePath = Paths.get(dirPath.toString(), shortName + date + ".log");
        Assertions.assertTrue(Files.exists(filePath));
        Logger logger = LoggerFactory.createLogger(userdir + outputDir, shortName);
        assertNotNull(logger);
        assertDoesNotThrow(()->{LoggerFactory.getLogManager().removeLogger(logger);});
    }

    @Test
    public void test_createLogger_2inputs_filename() throws IOException {
        String fileName = "log" + +random.nextLong(10000, 10000000);
        assertDoesNotThrow(()->{LoggerFactory.createLogger(dirPath.toString(), fileName+ ".log");});
        Assertions.assertTrue(Files.exists(dirPath));
        Path filePath = Paths.get(dirPath.toString(), fileName + date + ".log");
        System.out.println(filePath);
        Assertions.assertTrue(Files.exists(filePath));
        Logger logger = LoggerFactory.createLogger(userdir + outputDir, fileName);
        assertNotNull(logger);
        assertDoesNotThrow(()->{LoggerFactory.getLogManager().removeLogger(logger);});
    }

    @Test
    public void test_createLogger_3inputs_shortname() throws IOException {
        String shortname = "log" + +random.nextLong(10000, 10000000);
        int daysToLog = 5;
        assertDoesNotThrow(()->{LoggerFactory.createLogger(dirPath.toString(), shortname, daysToLog);});
        Assertions.assertTrue(Files.exists(dirPath));
        Path filePath = Paths.get(dirPath.toString(), shortname + date + ".log");
        System.out.println(filePath);
        Assertions.assertTrue(Files.exists(filePath));
        Logger logger = LoggerFactory.createLogger(userdir + outputDir, shortname);
        assertNotNull(logger);
        assertEquals(daysToLog, logger.getDaysToLog());
        assertDoesNotThrow(()->{LoggerFactory.getLogManager().removeLogger(logger);});
    }

    @Test
    public void test_createLogger_3inputs_filename() throws IOException {
        String fileName = "log" + +random.nextLong(10000, 10000000);
        int daysToLog = 5;
        assertDoesNotThrow(()->{LoggerFactory.createLogger(dirPath.toString(), fileName+ ".log", daysToLog);});
        Assertions.assertTrue(Files.exists(dirPath));
        Path filePath = Paths.get(dirPath.toString(), fileName + date + ".log");
        System.out.println(filePath);
        Assertions.assertTrue(Files.exists(filePath));
        Logger logger = LoggerFactory.createLogger(userdir + outputDir, fileName);
        assertNotNull(logger);
        assertEquals(daysToLog, logger.getDaysToLog());
        assertDoesNotThrow(()->{LoggerFactory.getLogManager().removeLogger(logger);});
    }

    @Test
    public void test_getLogger(){
        String shortName = "log" +random.nextLong(10000, 10000000);
        assertDoesNotThrow(()->{LoggerFactory.createLogger(dirPath.toString(), shortName);});
        Assertions.assertTrue(Files.exists(dirPath));
        Path filePath = Paths.get(dirPath.toString(), shortName + date + ".log");
        Assertions.assertTrue(Files.exists(filePath));
        assertNotNull(LoggerFactory.getLogger(shortName));
        assertDoesNotThrow(()->{LoggerFactory.getLogManager()
                .removeLogger(LoggerFactory.getLogger(shortName));});
    }

    @Test
    public void test_getLogger_logpath(){
        String shortName = "log" +random.nextLong(10000, 10000000);
        assertDoesNotThrow(()->{LoggerFactory.createLogger(dirPath.toString(), shortName);});
        Assertions.assertTrue(Files.exists(dirPath));
        Path filePath = Paths.get(dirPath.toString(), shortName + date + ".log");
        Assertions.assertTrue(Files.exists(filePath));
        assertNotNull(LoggerFactory.getLogger(shortName));
        assertDoesNotThrow(()->{LoggerFactory.getLogManager()
                .removeLogger(LoggerFactory.getLogger(shortName));});
    }

    @Test
    public void test_getLogger_logpath2(){
        String shortName = "log" +random.nextLong(10000, 10000000);
        assertDoesNotThrow(()->{LoggerFactory.createLogger(dirPath.toString(), shortName);});
        Assertions.assertTrue(Files.exists(dirPath));
        Path filePath = Paths.get(dirPath.toString(), shortName + date + ".log");
        Assertions.assertTrue(Files.exists(filePath));
        assertNotNull(LoggerFactory.getLoggerByPath(dirPath + "/" + shortName + ".log"));
        assertDoesNotThrow(()->{LoggerFactory.getLogManager()
                .removeLogger(LoggerFactory.getLoggerByPath(dirPath + "/" + shortName + ".log"));});
    }
}

