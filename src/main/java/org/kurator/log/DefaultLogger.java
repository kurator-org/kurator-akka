package org.kurator.log;

import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;

public class DefaultLogger implements Logger {
    
    private volatile Logger parent = null;
    private volatile PrintStream printStream = System.err;
    private volatile LogLevel level = LogLevel.WARN;
    private volatile boolean showTimestamps = true;
    private volatile boolean showLevel = true;
    private volatile boolean showSource = true;
    private volatile String source = "";
    private volatile String separator = ":";
    private volatile int maxMessageLength = 128;

    public void setParent(Logger parent) { this.parent = parent; }
    public void setLevel(LogLevel level) { this.level = level; }
    public void setPrintStream(PrintStream log) { this.printStream = log; }
    public void setSource(String tag) { this.source = tag; }
    public void setSeparator(String separator) { this.separator = separator; }
    public void setShowTimestamps(boolean st) { this.showTimestamps = st; }
    public void setShowLevel(boolean sl) { this.showLevel = sl; }
    public void setShowSource(boolean ss) { this.showSource = ss; }
    public void setMaxMessageLength(int m) { this.maxMessageLength = m; }
    
    public Logger getParent() { return this.parent; }
    public LogLevel getLevel() { return this.level; }
    public PrintStream getPrintStream() { return this.printStream; }
    public String getSource() { return source; }
    public String getSeparator() { return separator; }
    public boolean getShowTimestamps() { return this.showTimestamps; }
    public boolean getShowLevel() { return this.showLevel; }
    public boolean getShowSource() { return this.showSource; }
    public int getMaxMessageLength() { return this.maxMessageLength; }
    
    public Logger createChild() {
        Logger child = new DefaultLogger();
        child.setLevel(this.level);
        child.setParent(this);
        return child;
    }

    public synchronized void trace(String message) {
        if (level.value <= LogLevel.TRACE.value) log(LogLevel.TRACE, message, source);
    }

    public synchronized void trace(String message, String name, Object value) {
        trace(message + " [" + name + "=" + value + "]");
    }

    public synchronized void debug(String message) {
        if (level.value <= LogLevel.DEBUG.value) log(LogLevel.DEBUG, message, source);
    }
    
    public synchronized void debug(String message, String name, Object value) {
        debug(message + " [" + name + "=" + value + "]");
    }
    
    public synchronized void info(String message) {
        if (level.value <= LogLevel.INFO.value) log(LogLevel.INFO, message, source);
    }

    public synchronized void info(String message, String name, Object value) {
        info(message + " [" + name + "=" + value + "]");
    }
    
    public synchronized void warn(String message) {
        if (level.value <= LogLevel.WARN.value) log(LogLevel.WARN, message, source);
    }

    public synchronized void error(String message) {
        if (level.value <= LogLevel.ERROR.value) log(LogLevel.ERROR, message, source);
    }

    public synchronized void fatal(String message) {
        if (level.value <= LogLevel.FATAL.value) log(LogLevel.FATAL, message, source);
    }
   
    public synchronized void log(LogLevel level, String message, String source) {
       
        // delegate logging to parent if defined
        if (parent != null) { parent.log(level, message, source); return; }
       
        // otherwise build the log entry from the enabled log entry components
        StringBuffer entry = new StringBuffer();
        if (this.showTimestamps) { append(entry, pad(new Timestamp(new Date().getTime()).toString())); }
        if (this.showLevel) { append(entry, level.toString()); }
        if (this.showSource) { append(entry, source); }
        append(entry, getPrefix(message, maxMessageLength));
        
        // ...and then write the log entry
        this.printStream.println(entry);
    }
    
    private String pad(String timestamp) {
        if (timestamp.length() == 22) return timestamp += "0";
        if (timestamp.length() == 21) return timestamp += "00";
        return timestamp;
    }
    
    private void append(StringBuffer buffer, String s) {
        if (buffer.length() > 0 && !s.isEmpty()) buffer.append(separator);
        buffer.append(s);
    }
    
    private String getPrefix(String s, int prefixLength) {
        if (prefixLength < 1 || s.length() <= prefixLength) {
            return s;
        } else {
            return s.substring(0, prefixLength - 1);
        }
    }
}
