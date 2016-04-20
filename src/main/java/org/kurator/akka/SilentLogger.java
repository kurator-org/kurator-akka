package org.kurator.akka;

public class SilentLogger extends Logger {
    @Override public SilentLogger createChild() { return new SilentLogger(); }
    @Override public void debug(String m) {}
    @Override public void info(String m) {}
    @Override public void warn(String m) {}
    @Override public void error(String m) {}
    @Override public void fatal(String m) {}
    @Override public void log(LogLevel l, String m, String s) {}
}