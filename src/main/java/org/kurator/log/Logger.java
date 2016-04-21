package org.kurator.log;

public interface Logger {
    Logger createChild();
    void log(LogLevel l, String m, String s);
    void trace(String m);
    void value(String m);
    void value(String m, Object v);
    void value(String m, String n, Object v);
    void debug(String m);
    void info(String m);
    void warn(String m);
    void error(String m);
    void fatal(String m);
    void setLevel(LogLevel l);
    void setParent(Logger l);
    void setSource(String s);
}