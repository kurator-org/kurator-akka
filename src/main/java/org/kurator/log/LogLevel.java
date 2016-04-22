package org.kurator.log;

public enum LogLevel {
    
    ALL(0), TRACE(1), VALUE(2), COMM(3), DEBUG(4), INFO(5), WARN(6), ERROR(7), FATAL(8), OFF(9);
    
    final int value;
    LogLevel(int v) { this.value = v; }
    
    public static LogLevel toLogLevel(Object level) throws Exception {

        if (level instanceof LogLevel) return (LogLevel)level;
        
        if (level instanceof String) {
            String levelString = (String)level; 
            if (levelString.equalsIgnoreCase("ALL"))        return ALL;
            if (levelString.equalsIgnoreCase("A"))          return ALL;
            if (levelString.equalsIgnoreCase("TRACE"))      return TRACE;
            if (levelString.equalsIgnoreCase("T"))          return TRACE;
            if (levelString.equalsIgnoreCase("VALUE"))      return VALUE;
            if (levelString.equalsIgnoreCase("V"))          return VALUE;
            if (levelString.equalsIgnoreCase("COMM"))       return COMM;
            if (levelString.equalsIgnoreCase("C"))          return COMM;
            if (levelString.equalsIgnoreCase("DEBUG"))      return DEBUG;
            if (levelString.equalsIgnoreCase("D"))          return DEBUG;
            if (levelString.equalsIgnoreCase("INFO"))       return INFO;
            if (levelString.equalsIgnoreCase("I"))          return INFO;
            if (levelString.equalsIgnoreCase("WARNING"))    return WARN;
            if (levelString.equalsIgnoreCase("W"))          return WARN;
            if (levelString.equalsIgnoreCase("ERROR"))      return ERROR;
            if (levelString.equalsIgnoreCase("E"))          return ERROR;
            if (levelString.equalsIgnoreCase("FATAL"))      return FATAL;
            if (levelString.equalsIgnoreCase("F"))          return FATAL;
            if (levelString.equalsIgnoreCase("CRITICAL"))   return FATAL;
            if (levelString.equalsIgnoreCase("C"))          return FATAL;
            if (levelString.equalsIgnoreCase("OFF"))        return OFF;
            if (levelString.equalsIgnoreCase("O"))          return OFF;
        }

        throw new Exception("Unrecognized LogLevel: " + level);
    }
}