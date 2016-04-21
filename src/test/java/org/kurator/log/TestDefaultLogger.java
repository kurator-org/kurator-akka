package org.kurator.log;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.log.DefaultLogger;
import org.kurator.log.LogLevel;

public class TestDefaultLogger extends KuratorAkkaTestCase {

    private OutputStream logBuffer;
    private PrintStream logStream;
    private DefaultLogger logger;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        logBuffer = new ByteArrayOutputStream();
        logStream = new PrintStream(logBuffer);
        logger = new DefaultLogger();
        logger.setPrintStream(logStream);
        logger.setShowTimestamps(false);
        logger.setShowLevel(false);
    }
    
    public void testLogger_LevelCritical() {
        logger.setLevel(LogLevel.FATAL);
        logger.debug("debug");
        logger.info("info");
        logger.warn("warning");
        logger.error("error");
        logger.fatal("critical");
        assertEquals(
                "critical"  + EOL,
                logBuffer.toString());
    }
    
    public void testLogger_LevelError() {
        logger.setLevel(LogLevel.ERROR);
        logger.debug("debug");
        logger.info("info");
        logger.warn("warning");
        logger.error("error");
        logger.fatal("critical");
        assertEquals(
                "error"     + EOL +
                "critical"  + EOL,
                logBuffer.toString());
    }
    
    public void testLogger_LevelWarning() {
        logger.setLevel(LogLevel.WARN);
        logger.debug("debug");
        logger.info("info");
        logger.warn("warning");
        logger.error("error");
        logger.fatal("critical");
        assertEquals(
                "warning"   + EOL +
                "error"     + EOL +
                "critical"  + EOL,
                logBuffer.toString());
    }

    public void testLogger_LevelDebug() {
        logger.setLevel(LogLevel.DEBUG);
        logger.debug("debug");
        logger.info("info");
        logger.warn("warning");
        logger.error("error");
        logger.fatal("critical");
        assertEquals(
                "debug"     + EOL +
                "info"      + EOL + 
                "warning"   + EOL +
                "error"     + EOL +
                "critical"  + EOL,
                logBuffer.toString());
    }
    
    public void testLogger_LevelAll() {
        logger.setLevel(LogLevel.ALL);
        logger.debug("debug");
        logger.info("info");
        logger.warn("warning");
        logger.error("error");
        logger.fatal("critical");
        assertEquals(
                "debug"     + EOL +
                "info"      + EOL + 
                "warning"   + EOL +
                "error"     + EOL +
                "critical"  + EOL,
                logBuffer.toString());
    }
}
