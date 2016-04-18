package org.kurator.akka;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class TestLogger extends KuratorAkkaTestCase {

    private OutputStream logBuffer;
    private PrintStream logStream;
    private Logger logger;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        logBuffer = new ByteArrayOutputStream();
        logStream = new PrintStream(logBuffer);
        logger = new Logger();
        logger.setLog(logStream);
        logger.setTimestamp(false);
    }
    
    public void testLogger_LevelNone() {
        logger.setLevel(Logger.LogLevel.NONE);
        logger.log("log");
        logger.info("info");
        logger.warning("warning");
        logger.error("error");
        logger.critical("critical");
        assertEquals(
                "log"       + EOL, 
                logBuffer.toString());
    }
    
    public void testLogger_LevelInfo() {
        logger.setLevel(Logger.LogLevel.INFO);
        logger.log("log");
        logger.info("info");
        logger.warning("warning");
        logger.error("error");
        logger.critical("critical");
        assertEquals(
                "log"       + EOL +
                "info"      + EOL,
                logBuffer.toString());
    }
    
    public void testLogger_LevelWarning() {
        logger.setLevel(Logger.LogLevel.WARNING);
        logger.log("log");
        logger.info("info");
        logger.warning("warning");
        logger.error("error");
        logger.critical("critical");
        assertEquals(
                "log"       + EOL +
                "info"      + EOL + 
                "warning"   + EOL,
                logBuffer.toString());
    }

    public void testLogger_LevelError() {
        logger.setLevel(Logger.LogLevel.ERROR);
        logger.log("log");
        logger.info("info");
        logger.warning("warning");
        logger.error("error");
        logger.critical("critical");
        assertEquals(
                "log"       + EOL +
                "info"      + EOL + 
                "warning"   + EOL +
                "error"     + EOL,
                logBuffer.toString());
    }
    
    public void testLogger_LevelCritical() {
        logger.setLevel(Logger.LogLevel.CRITICAL);
        logger.log("log");
        logger.info("info");
        logger.warning("warning");
        logger.error("error");
        logger.critical("critical");
        assertEquals(
                "log"       + EOL +
                "info"      + EOL + 
                "warning"   + EOL +
                "error"     + EOL +
                "critical"  + EOL,
                logBuffer.toString());
    }
}
