package org.kurator.log;

public class SilentLogger implements Logger {
    @Override public SilentLogger createChild() { return new SilentLogger(); }
    @Override public void trace(String message) {}
    @Override public void debug(String m) {}
    @Override public void info(String m) {}
    @Override public void warn(String m) {}
    @Override public void error(String m) {}
    @Override public void fatal(String m) {}
    @Override public void setParent(Logger p) {}
    @Override public void setLevel(LogLevel l)  {}
    @Override public void setSource(String s) {}
    @Override public void value(String m) {}
    @Override public void value(String m, Object v) {}
    @Override public void value(String m, String n, Object v) {}
    @Override public void log(LogLevel l, String m, String s) {}
}