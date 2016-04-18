package org.kurator.akka;

import java.io.PrintStream;
import java.sql.Timestamp;

public class Logger {

    public static enum LogLevel {
        NONE(0), INFO(1), WARNING(2), ERROR(3), CRITICAL(4);
        public final int value;
        LogLevel(int v) { this.value = v; }
    }
    
    private volatile PrintStream log = System.err;
    private volatile LogLevel level = LogLevel.CRITICAL;
    private volatile boolean timestamp = true;

    public void setLevel(LogLevel level) { this.level = level; }
    public void setLog(PrintStream log) { this.log = log; }
    public void setTimestamp(boolean timestamp) { this.timestamp = timestamp; }
    
    public LogLevel getLevel() { return this.level; }
    public PrintStream getLog() { return this.log; }
    public boolean getTimestamps() { return this.timestamp; }
    
   public synchronized void info(String message) {
       if (level.value >= LogLevel.INFO.value) log(message);
   }
   
   public synchronized void warning(String message) {
       if (level.value >= LogLevel.WARNING.value) log(message);
   }

   public synchronized void error(String message) {
       if (level.value >= LogLevel.ERROR.value) log(message);
   }

   public synchronized void critical(String message) {
       if (level.value >= LogLevel.CRITICAL.value) log(message);
   }
   
   public synchronized void log(String message) {
       
       if (this.timestamp) {
           java.util.Date date= new java.util.Date();
           message = String.format("%s %s", new Timestamp(date.getTime()), message);
       }
       
       log.println(message);
   }
}
